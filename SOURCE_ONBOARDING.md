# Adding a content source

1. Record the provider, official API, exact asset hosts and attribution in `SOURCES_AND_RIGHTS.md`.
2. Establish stream, download, offline, record and share permissions separately. Unknown permissions are false; a discovered URL is not permission.
3. Allocate a stable source ID and independent asset source ID for third-party redirects. Add a disabled catalog record first.
4. Implement the existing remote datasource/repository contract and map stable IDs; never duplicate playback or download engines.
5. Add DTO parsing and resolver tests, including partial mushafs and malformed URLs. Verify several real assets and their redirects.
6. Enable only documented capabilities in `PlannedSourceCatalog` after approval and retest rights enforcement in the recorder and download manager.
7. Update API_SOURCES.md, SOURCES_AND_RIGHTS.md, the attribution UI and technical/rights check dates.
