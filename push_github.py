import os, subprocess

token = os.environ.get('GITHUB_TOKEN', '')
if not token:
    with open('C:\\Users\\Dell\\AppData\\Local\\hermes\\.env') as f:
        for line in f:
            line = line.strip()
            if line.startswith('GITHUB_TOKEN=') and not line.startswith('#') and 'ghp_' in line:
                token = line.split('=', 1)[1]
                break

project_dir = 'C:\\Users\\Dell\\ClearConsent'

# Set remote
subprocess.run(['git', 'remote', 'rm', 'origin'], cwd=project_dir, capture_output=True)
remote_url = f'https://nitin-121:{token}@github.com/nitin-121/ClearConsent.git'
subprocess.run(['git', 'remote', 'add', 'origin', remote_url], cwd=project_dir)
print("Remote set ok")

# Add all
r = subprocess.run(['git', 'add', '-A'], cwd=project_dir, capture_output=True, text=True)
print(f"Add: exit={r.returncode}")

# Status
r = subprocess.run(['git', 'status', '--porcelain'], cwd=project_dir, capture_output=True, text=True)
files = [l for l in r.stdout.strip().split('\n') if l]
print(f"Files staged: {len(files)}")

# Commit
msg = 'Initial release: ClearConsent v1.0\n\nPrivacy-first Android meeting notes app\n- Record -> Transcribe -> Summarize -> Deliver closed-loop pipeline\n- MVVM + Clean Architecture with Hilt DI\n- Room + SQLCipher for encrypted local storage\n- Jetpack Compose + Material 3 UI\n- Biometric authentication support\n- Foreground recording service with persistent notification\n- Daily digest via WorkManager'
r = subprocess.run(['git', 'commit', '-m', msg], cwd=project_dir, capture_output=True, text=True)
print(f"Commit: exit={r.returncode}")
print(f"  {r.stdout.strip()[:300]}")

# Push
r = subprocess.run(['git', 'push', '-u', 'origin', 'main'], cwd=project_dir, capture_output=True, text=True, timeout=60)
print(f"Push: exit={r.returncode}")
print(f"  {r.stdout.strip()[:300]}")
if r.stderr:
    print(f"  stderr: {r.stderr.strip()[:300]}")
