# Test Traceability Matrix

| Requirement or rule | Layer | Test class | Test method |
|---|---|---|---|
| Hive name must be unique | Service | `HiveServiceImplTest` | `createHive_throwsWhenDuplicateNameAttempted` |
| Hive name length must be 2-50 characters | Service | `HiveServiceImplTest` | `createHive_throwsWhenNameLengthOutsideBounds` and `createHive_acceptsBoundaryValidNameLengths` |
| Hive location length must be 2-80 characters | Service | `HiveServiceImplTest` | `createHive_throwsWhenLocationLengthOutsideBounds` and `createHive_acceptsBoundaryValidLocationLengths` |
| Hive cannot be deleted when readings exist | Service | `HiveServiceImplTest` | `deleteById_throwsWhenReadingsStillExistForThatHive` |
| Temperature must be between -9.0 and +46.5 C | Service | `TemperatureReadingServiceImplTest` | `recordReading_throwsWhenTemperatureOutsideBounds` and `recordReading_acceptsBoundaryTemperatures` |
| recordedAt is required | Service | `TemperatureReadingServiceImplTest` | `recordReading_throwsWhenRecordedAtIsNull` |
| Minutes must be between 1 and 1440 | Service | `TemperatureReadingServiceImplTest` | `averageTempLastMinutes_throwsWhenMinutesOutsideBounds` and `averageTempLastMinutes_acceptsBoundaryMinuteValues` |
| Delta must be between -20.0 and +20.0 | Service | `TemperatureReadingServiceImplTest` | `applyOffsetToHive_throwsWhenDeltaOutsideBounds` and `applyOffsetToHive_acceptsBoundaryDeltaValues` |
| Reading reassignment must be blocked when target hive already has the same timestamp | Service | `TemperatureReadingServiceImplTest` | `assignToHive_throwsWhenTargetHiveAlreadyHasReadingAtSameTimestamp` |