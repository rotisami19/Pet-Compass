# Quick Start Guide - Publishing Pet Compass to CurseForge

## Step 1: Configure CurseForge Credentials

Run the configuration script:
```powershell
.\configure-curseforge.ps1
```

Or double-click:
```
configure-curseforge.bat
```

This will prompt you to enter:
- **API Key** from https://authors.curseforge.com/account/api-tokens
- **Project ID** from your CurseForge project page

## Step 2: Test Configuration (Optional)

Verify your credentials are set:
```powershell
$env:CURSEFORGE_API_KEY    # Should show your API key
$env:CURSEFORGE_PROJECT_ID # Should show your project ID
```

## Step 3: Publish All Branches

Run the publish script:
```powershell
.\publish-all-branches.ps1
```

### Options:
- **Publish everything:**
  ```powershell
  .\publish-all-branches.ps1
  ```

- **Dry run (preview only):**
  ```powershell
  .\publish-all-branches.ps1 -DryRun
  ```

- **Publish single branch manually:**
  ```powershell
  git checkout main
  .\gradlew.bat build --no-daemon
  .\gradlew.bat curseforge --no-daemon
  ```

## Branches Being Published

| Branch | Minecraft | Loader | Status |
|--------|-----------|--------|--------|
| main | 1.21.1 | NeoForge | ✓ Configured |
| 1.20.x-neoforge | 1.20.1 | NeoForge | ✓ Configured |
| 1.20.x-forge | 1.20.1 | Forge | ✓ Configured |

## Checklist Before Publishing

- [ ] API Key obtained from CurseForge
- [ ] Project ID copied from CurseForge URL
- [ ] All local changes committed or stashed
- [ ] All branches are up-to-date (`git fetch`)
- [ ] Version numbers set correctly in each branch
- [ ] Changelog updated

## Troubleshooting

### Publication fails with "Missing CURSEFORGE_API_KEY"
→ Run `.\configure-curseforge.ps1` to set credentials

### Build fails on one branch
→ Check out that branch and run: `.\gradlew.bat build`
→ Fix any compilation errors, then rerun `.\publish-all-branches.ps1`

### ModLoader not found in build.gradle
→ Different branches use different loaders (Forge vs NeoForge)
→ Each build.gradle is already configured correctly for its branch

## Additional Information

- **Detailed Help:** See `PUBLISH-CURSEFORGE.md`
- **Build Manual:** `./gradlew.bat build`
- **Clean Build:** `./gradlew.bat clean build`
- **Check Version:** Look at `gradle.properties`

---

Questions? Check:
- https://docs.curseforge.com/authors/getting-started/
- https://github.com/Darkhax/CurseForgeGradle
