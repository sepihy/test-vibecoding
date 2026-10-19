# WhatsApp Channel public adapter

Channel: https://whatsapp.com/channel/0029Vb3Xp1CInlqTzXlogg0h

## What is implemented
- Makes a direct HTTPS GET to the public channel landing URL.
- Follows normal redirects and records HTTP status, final URL, public title/description.
- Scans only HTML that the public page actually returns for possible price-related text.
- Does not log in, call private endpoints, evade bot checks, or claim posts were read if only landing-page metadata is visible.

## Important limitation
WhatsApp Channel pages may expose only a landing/preview page and not the channel's full post feed in ordinary public HTML. If so, adapter returns PUBLIC_PAGE_ONLY and does not create price observations. This is the honest result, not a successful price-feed integration.

## How to test
Install/run the Android app and tap “آزمایش اتصال مستقیم”. The test happens on-device because network accessibility may differ by region/device. HTTP status and any public metadata/candidate lines are shown. Candidate lines are NOT prices until part identity, currency, freshness, and independent-source rules pass.

## Next step if posts are not exposed
Use an officially supported export/share path or provide post screenshots/files. Do not use unofficial private endpoints or session-cookie harvesting.
