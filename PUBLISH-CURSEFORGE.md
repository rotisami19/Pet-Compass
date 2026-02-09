# Publishing to CurseForge

This guide explains how to publish all branches of Pet Compass to CurseForge.

## Prerequisites

1. **CurseForge Account & API Key**
   - Go to https://authors.curseforge.com/account/api-tokens
   - Create a new API token
   - Save it securely

2. **Environment Variables**
   - `CURSEFORGE_API_KEY`: Your CurseForge API token
   - `CURSEFORGE_PROJECT_ID`: Your project ID (found in your project URL)

## Setting Environment Variables (PowerShell)

### Option 1: Temporary (current session only)
```powershell
$env:CURSEFORGE_API_KEY = "your-api-key-here"
$env:CURSEFORGE_PROJECT_ID = "your-project-id"
```

### Option 2: Permanent (for all sessions)
```powershell
[Environment]::SetEnvironmentVariable("CURSEFORGE_API_KEY", "your-api-key-here", "User")
[Environment]::SetEnvironmentVariable("CURSEFORGE_PROJECT_ID", "your-project-id", "User")
```

After setting permanently, restart PowerShell or VS Code.

## Usage

### Publish All Branches
```powershell
.\publish-all-branches.ps1
```

### Dry Run (preview what would happen)
```powershell
.\publish-all-branches.ps1 -DryRun
```

## Branches & Versions

The script will publish the following branches:

| Branch | Minecraft | Loader | Current Version |
|--------|-----------|--------|-----------------|
| main | 1.21.1 | NeoForge | 1.1.0 |
| 1.20.x-neoforge | 1.20.1 | NeoForge | 1.1.0 |
| 1.20.x-forge | 1.20.1 | Forge | 1.1.0 |

## What the Script Does

1. ✓ Checks for required API credentials
2. ✓ Stashes uncommitted changes
3. ✓ For each branch:
   - Checks out the branch
   - Pulls latest from remote
   - Builds with `./gradlew.bat build`
   - Publishes with `./gradlew.bat curseforge`
4. ✓ Returns to original branch
5. ✓ Restores stashed changes
6. ✓ Reports success/failure

## Manual Publishing (Single Branch)

If you want to publish just one branch:

```powershell
# 1. Make sure you're on the branch
git checkout branch-name

# 2. Build the mod
.\gradlew.bat build --no-daemon

# 3. Publish to CurseForge
.\gradlew.bat curseforge --no-daemon
```

## Troubleshooting

### "Build failed for [branch]"
- Ensure the branch builds locally: `.\gradlew.bat build`
- Check that gradle.properties has correct versions
- Verify no compilation errors exist

### "CurseForge upload failed"
- Verify API key is correct
- Verify project ID is correct
- Check CurseForge project settings allow automated uploads
- Ensure you have permission to upload to this project

### "Failed to checkout branch"
- Ensure the branch exists: `git branch -a`
- Ensure you have no uncommitted changes preventing checkout

## Version Management Note

Currently all branches use version `1.1.0`. If you want different versions per Minecraft version, update:
- `gradle.properties` in each branch with different `mod_version` values
- Or modify the script to append version suffixes (e.g., `1.1.0-1.20.1`)
