"""Conservative source export policy and basic checks; not a full secret scanner."""
from pathlib import Path
import ipaddress
import json
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
TOP_FILES = {'.gitignore', '.gitattributes', '.editorconfig', 'README.md', 'LICENSE',
             'SECURITY.md', 'CONTRIBUTING.md', 'THIRD_PARTY_NOTICES.md', 'CHANGELOG.md'}
DIRECTORIES = {'backend', 'admin-android', 'reviewer-android', 'deploy', 'docs', 'scripts', '.github'}
BLOCKED_PARTS = {'.git', '.backup', '.local', '.gradle', '.kotlin', '.idea', 'build', 'target',
                 '__pycache__', 'dist', 'releases'}
EXTENSIONS = {'.java', '.kt', '.kts', '.xml', '.yml', '.yaml', '.json', '.html', '.css', '.js',
              '.md', '.py', '.sh', '.properties', '.pro', '.txt'}
SPECIAL = {'.gitignore', '.dockerignore', '.env.example', 'Dockerfile', 'org.mockito.plugins.MockMaker'}


def allowed(relative):
    if any(part in BLOCKED_PARTS for part in relative.parts):
        return False
    if relative.name == 'local.properties' or (relative.name.startswith('.env') and relative.name != '.env.example'):
        return False
    if len(relative.parts) == 1:
        return relative.name in TOP_FILES or relative.name == "SOURCE_MANIFEST.json"
    return (relative.parts[0] in DIRECTORIES
            and (relative.suffix in EXTENSIONS or relative.name in SPECIAL))


def source_files():
    # Do not descend into local history/caches (which may be very large).
    def walk(directory):
        for path in sorted(directory.iterdir()):
            if path.is_symlink():
                continue
            if path.is_dir():
                if path.name not in BLOCKED_PARTS:
                    yield from walk(path)
            elif allowed(path.relative_to(ROOT)):
                yield path
    for name in sorted(TOP_FILES):
        path = ROOT / name
        if path.is_file() and not path.is_symlink():
            yield path
    for name in sorted(DIRECTORIES):
        path = ROOT / name
        if path.is_dir() and not path.is_symlink():
            yield from walk(path)


def check():
    files = list(source_files())
    errors = []
    for name in TOP_FILES:
        if not (ROOT / name).is_file(): errors.append(f'Missing required file: {name}')
    patterns = [
        r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----',
        r'\b(?:ghp_|github_pat_)[A-Za-z0-9_]{30,}',
        r'\bAKIA[A-Z0-9]{16}\b',
        r'/storage/emulated/\d/',
        r'android\.aapt2FromMavenOverride=',
        r'sshpass\s',
    ]
    for path in files:
        rel = path.relative_to(ROOT)
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeError:
            errors.append(f'Unexpected non-UTF8 file: {rel}')
            continue
        if path.stat().st_size > 2 * 1024 * 1024:
            errors.append(f'Oversized source file: {rel}')
        for pattern in patterns:
            if re.search(pattern, text): errors.append(f'Suspicious content in {rel} (review locally)')
        for value in re.findall(r'https?://((?:\d{1,3}\.){3}\d{1,3})(?=[:/\s\'"`]|$)', text):
            if ipaddress.ip_address(value).is_global:
                errors.append(f'Public numeric server address in {rel}')
        if path.name == '.env.example':
            for line in text.splitlines():
                if re.match(r'^(DB_PASSWORD|MYSQL_ROOT_PASSWORD|ADMIN_PASSWORD|CLIENT_SECRET|AFDIAN_TOKEN)=.+', line):
                    errors.append(f'Non-empty example secret in {rel}')
    spec = json.loads((ROOT / 'backend/src/main/resources/static/openapi.json').read_text())
    for route in ['/api/overview', '/api/dashboard']:
        if spec['paths'][route]['get'].get('security') != [{'admin': []}]:
            errors.append(f'Legacy route docs lack admin authentication: {route}')
    if 'passwordHash' in spec['components']['schemas']['UserAccount']['properties']:
        errors.append('OpenAPI exposes passwordHash')
    web = (ROOT / 'backend/src/main/java/com/usermonitor/server/security/WebConfig.java').read_text()
    if not all('"' + route + '"' in web for route in ['/api/overview', '/api/dashboard']):
        errors.append('Legacy routes missing from actual interceptor configuration')
    for module in ['admin-android', 'reviewer-android']:
        manifest = (ROOT / module / 'app/src/main/AndroidManifest.xml').read_text()
        if 'android:usesCleartextTraffic="false"' not in manifest:
            errors.append(f'{module}: cleartext traffic not disabled')
    # A .gitignore is not sufficient if local files were previously tracked.
    if (ROOT / '.git').exists():
        result = subprocess.run(['git', '-C', str(ROOT), 'ls-files', '-z'], capture_output=True)
        if result.returncode != 0:
            errors.append('Could not inspect tracked Git files')
        else:
            for name in result.stdout.decode().split('\0'):
                if name and not allowed(Path(name)):
                    errors.append(f'Non-publishable tracked file: {name}')
    if errors:
        raise SystemExit('\n'.join(errors))
    print(f'PASS: {len(files)} publishable UTF-8 source files; basic checks passed.')
    print('Scope: current allowlisted source only, not full Git history, binary analysis or dependency audit.')
    return files


if __name__ == '__main__':
    check()