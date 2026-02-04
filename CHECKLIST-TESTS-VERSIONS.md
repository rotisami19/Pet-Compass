# PetCompass testing checklist – before release

Validate **each build** (each branch) before publishing to CurseForge. You have 3 builds, not one per Minecraft version number: the 1.20.x NeoForge JAR is published for 1.20, 1.20.1, 1.20.2, 1.20.4, 1.20.6 on CurseForge.

## How to test each version

1. **Automated build**: run `.\test-all-versions.ps1` to verify each branch compiles. The script auto-stashes your local changes and restores them at the end.
2. **Manual test**: for each branch, run `git checkout <branch>`, then `.\gradlew runClient`, and tick the checklist below.

---

## Per version

### 1.20.x NeoForge (branch `1.20.x-neoforge`)
- [ ] Game starts without crash
- [ ] Pet Compass item appears in creative inventory / can be crafted
- [ ] Pet Compass recipe works
- [ ] Right-click on compass opens pet interface
- [ ] Tamed pets appear in the list
- [ ] Selecting a pet updates direction / distance
- [ ] Compass points to selected pet
- [ ] Glowing effect on tracked pet when nearby
- [ ] Pets in other dimensions visible (Nether / End if tested)
- [ ] No errors in logs (F3 + check console)

### 1.20.x Forge (branch `1.20.x-forge`)
- [ ] Same checklist as 1.20.x NeoForge above

### 1.21.x NeoForge (branch `1.21.x-neoforge`)
- [ ] Same checklist as 1.20.x NeoForge above
- [ ] Verify recipes / 1.21 registries if API changes

---

## Quick summary

| Branch           | Build OK | Client test OK | Ready to publish |
|------------------|----------|----------------|------------------|
| 1.20.x-neoforge  |          |                |                  |
| 1.20.x-forge     |          |                |                  |
| 1.21.x-neoforge  |          |                |                  |

---

## Useful commands

```powershell
# Test builds for all branches
.\test-all-versions.ps1

# Run client for current branch
.\gradlew runClient

# Switch back to your working branch after testing
git checkout 1.20.x-neoforge
```

Once each version is checked and validated, you can publish with confidence.
