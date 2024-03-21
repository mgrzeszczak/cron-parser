# cron-parser

This project implements a cron expression parser in Kotlin.

## How to build
```
./gradlew clean build
```

## How to use
```
java -jar build/libs/cron-parser-1.0-all.jar "*/15 0 1,15 * 1-5 /usr/bin/find"
```

## Supported features

### Syntax:
- number value
- any value (*)
- step expression with starting value
  - */5
  - 3/5
- range expression (1-5)
- month named values (JAN-DEC)
- day of week named values (MON-SUN)

### Allowed value ranges:
- minute: 0-59
- hour: 0-23
- day of month: 1-31
- month: 1-12, JAN-DEC
- day of week: 0-6, MON-SUN
