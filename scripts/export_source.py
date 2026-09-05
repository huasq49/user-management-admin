"""Export only reviewed source types; never package the working directory wholesale."""
import hashlib
import json
import zipfile
from release_check import ROOT, check

files = check()
out = ROOT / 'dist'
out.mkdir(exist_ok=True)
archive = out / 'user-monitor-source.zip'
manifest = {}
with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as bundle:
    for path in sorted(files):
        name = path.relative_to(ROOT).as_posix()
        data = path.read_bytes()
        manifest[name] = hashlib.sha256(data).hexdigest()
        info = zipfile.ZipInfo('user-monitor/' + name, date_time=(2026, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = 0o100644 << 16
        bundle.writestr(info, data)
    info = zipfile.ZipInfo('user-monitor/SOURCE_MANIFEST.json', date_time=(2026, 1, 1, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = 0o100644 << 16
    bundle.writestr(info, json.dumps(manifest, ensure_ascii=False, indent=2) + '\n')
digest = hashlib.sha256(archive.read_bytes()).hexdigest()
(archive.with_suffix('.zip.sha256')).write_text(f'{digest}  {archive.name}\n')
print(f'Exported {len(files)} source files: {archive.name} ({archive.stat().st_size} bytes)')
print('SHA256:', digest)