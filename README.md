# miku-xlsx2md-java

Java port of [`igapyon/miku-xlsx2md`](https://github.com/igapyon/miku-xlsx2md) following the straight conversion rules in [docs/miku-straight-conversion-guide.md](docs/miku-straight-conversion-guide.md).

## Fixed Baseline

- Java source / target compatibility: `1.8`
- Build tool: `Maven`
- Test framework: `JUnit Jupiter`
- Primary test entrypoint: `mvn test`
- Packaging: single fat jar
- Local upstream workspace: `workplace/`

## Current Status

- Upstream source / test / CLI inventory completed from `workplace/miku-xlsx2md`
- Initial Java multi-module scaffolding is in place
- Straight-converted utility modules implemented:
  - `address-utils.ts`
  - `markdown-normalize.ts`
  - `markdown-escape.ts`
- Additional option / encoding modules implemented:
  - `markdown-options.ts`
  - `text-encoding.ts`
- Java CLI skeleton is implemented with Node-compatible option vocabulary
- Maven plugin skeleton is implemented
- Workbook parsing / conversion / ZIP export are not implemented yet

## Build

```bash
mvn test
```

```bash
mvn package
```

The shaded CLI jar is produced under `miku-xlsx2md/target/`.

## CLI

Current entrypoint:

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.1.0-SNAPSHOT.jar --help
```

The CLI already validates the main option set used by the upstream Node.js CLI, but workbook conversion is still pending.

## Documents

- [docs/upstream-class-mapping.md](docs/upstream-class-mapping.md)
- [docs/upstream-test-mapping.md](docs/upstream-test-mapping.md)
- [docs/remaining-items.md](docs/remaining-items.md)
- [docs/follow-up-log.md](docs/follow-up-log.md)
- [TODO.md](TODO.md)
