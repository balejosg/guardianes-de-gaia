
import os

lcov_path = '/home/run0/guardianes/guardianes-de-gaia/mobile/guardianes_mobile/coverage/lcov.info'
if not os.path.exists(lcov_path):
    print(f'Error: {lcov_path} not found')
    exit(1)

files = {}
current_file = None
current_lh = 0
current_lf = 0

with open(lcov_path, 'r') as f:
    for line in f:
        line = line.strip()
        if line.startswith('SF:'):
            current_file = line[3:]
        elif line.startswith('LH:'):
            current_lh = int(line[3:])
        elif line.startswith('LF:'):
            current_lf = int(line[3:])
        elif line == 'end_of_record':
            if current_file:
                files[current_file] = {'lh': current_lh, 'lf': current_lf}
            current_file = None

results = []
for f, data in files.items():
    if data['lf'] > 0:
        missed = data['lf'] - data['lh']
        coverage = (data['lh'] / data['lf']) * 100
        results.append((missed, coverage, f, data['lf']))

results.sort(key=lambda x: x[0], reverse=True)

print(f'{'Missed':<10} {'Coverage':<10} {'Total':<10} {'File'}')
print('-' * 100)
for missed, coverage, f, total in results[:30]:
    print(f'{missed:<10} {coverage:>8.1f}% {total:<10} {f}')
