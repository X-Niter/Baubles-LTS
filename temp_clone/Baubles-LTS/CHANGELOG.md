# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://docs.minecraftforge.net/en/1.12.x/conventions/versioning/).

## Unreleased

### Added
- Added `IBauble#playEquipSound` for Server side equipping sound identifiers which defaults to Generic armor equip sound.
- Added `IBauble#canRightClickEquip`, default true, almost all baubles should be right click equip-able now.
- `Alpha` Curio GUI implementation, default disabled, __Expect Bugs__

### Changed
- Updated dev environment to the latest CleanRoom changes.
- Mod versioning to include API Identifier.
- updated pt_br.lang [#343](https://github.com/Azanor/Baubles/pull/343)

### Deprecated

### Removed

### Fixed
- Fixed Example Baubles Ring translation key for item name.
- Fix mod versioning to include API Identifier.
- 

### Security

## 1.12.2-1.0.6.0
- XNiter(Me) Created fork of Baubles
- Maintaining group & class schema to allow drop in replacement
- Forge Gradle Update to 5
- Using CleanroomMC FG5 env build
- Added FancyGradle to build.gradle
- Update to Forge `1.12.2-14.23.5.2860`
- Versioning Schema is [Forge Recommended Versioning](https://docs.minecraftforge.net/en/1.12.x/conventions/versioning/)
- Update Registrations (what?)
- Update pl_pl.lang Thanks to(Pabilo8)
- Update tr_tr.lang Thanks to(RoCoKo)
- Update ru_ru.lang Thanks to(DrHesperus)
- Update uk_ua.lang Thanks to(burunduk)
- Added en_ud.lang Thanks to(The-Fireplace, X_Niter fixed spelling)
- Added ja_jp.lang Thanks to(2z6c)
- Added sv_se.lang, Thanks to(Regnander)
- Added BaubleEquipmentChangeEvent [#292](https://github.com/Azanor/Baubles/pull/292)
- Maintaining the [License](https://creativecommons.org/licenses/by-nc-sa/3.0/)
- Credit: [Original Baubles BY:Azanor13](https://www.curseforge.com/minecraft/mc-mods/baubles)

## 1.5.2
- API: added isBaubleEquipped helper method to BaublesAPI
- fixed player bauble syncing (Thanks pau101) closes #235

## 1.5.1
- added IBauble as a capability (see https://github.com/Azanor/Baubles/pull/208)

## 1.5.0
- updated for MC 1.12