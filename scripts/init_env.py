"""Generate local deployment secrets without printing them or overwriting existing data."""
from pathlib import Path
import os
import secrets

root = Path(__file__).resolve().parents[1]
target = root / 'deploy/.env'
values = {key: secrets.token_hex(32) for key in
          ('DB_PASSWORD', 'MYSQL_ROOT_PASSWORD', 'ADMIN_PASSWORD', 'CLIENT_SECRET')}
text = (root / 'deploy/.env.example').read_text()
for key, value in values.items():
    text = text.replace(key + '=\n', key + '=' + value + '\n')
try:
    fd = os.open(target, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
except FileExistsError:
    raise SystemExit('deploy/.env already exists; refusing to overwrite')
with os.fdopen(fd, 'w') as output:
    output.write(text)
print('Created deploy/.env; never commit or share this file. Review DDL_AUTO before first boot.')
