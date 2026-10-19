# هفته | Hafte Personal Planner

This directory is a self-contained WordPress plugin for the Persian RTL productivity workspace designed in `hafte-standalone.html`.

## Installation

1. Copy the `hafte` directory into `wp-content/plugins/` (or upload the zip from WordPress admin).
2. Activate **Hafte Personal Planner** in **Plugins**.
3. Create a page and add the shortcode:

   ```text
   [hafte]
   ```

4. Publish the page.

The plugin has no build step and no third-party runtime dependency. Logged-in users get state synchronization through the WordPress REST API and user metadata. Visitors get the same experience with browser local storage.
