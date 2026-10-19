<?php
/**
 * Plugin Name: Hafte Personal Planner
 * Description: A Persian, RTL productivity workspace inspired by the Hafte SaaS design. Add [hafte] to any page.
 * Version: 1.0.0
 * Author: Hafte
 * Requires at least: 6.2
 * Requires PHP: 7.4
 * License: GPL-2.0-or-later
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Hafte_Plugin {
    const VERSION = '1.0.0';
    const META_KEY = '_hafte_workspace_state';

    public function __construct() {
        add_action('wp_enqueue_scripts', [$this, 'register_assets']);
        add_shortcode('hafte', [$this, 'shortcode']);
        add_action('rest_api_init', [$this, 'register_rest_routes']);
    }

    public function register_assets() {
        $base = plugin_dir_url(__FILE__);
        wp_register_style('hafte', $base . 'assets/hafte.css', [], self::VERSION);
        wp_register_script('hafte', $base . 'assets/hafte.js', [], self::VERSION, true);

        wp_localize_script('hafte', 'HafteConfig', [
            'apiUrl' => esc_url_raw(rest_url('hafte/v1/state')),
            'nonce' => wp_create_nonce('wp_rest'),
            'loggedIn' => is_user_logged_in(),
            'storageKey' => 'hafte-workspace-v1',
        ]);
    }

    public function shortcode($atts = []) {
        wp_enqueue_style('hafte');
        wp_enqueue_script('hafte');

        ob_start();
        ?>
        <div class="hafte-app" dir="rtl" data-hafte-app>
            <div class="hafte-loading" role="status">در حال آماده‌سازی فضای هفته…</div>
        </div>
        <?php
        return ob_get_clean();
    }

    public function register_rest_routes() {
        register_rest_route('hafte/v1', '/state', [
            [
                'methods' => WP_REST_Server::READABLE,
                'callback' => [$this, 'read_state'],
                'permission_callback' => [$this, 'can_read_state'],
            ],
            [
                'methods' => WP_REST_Server::CREATABLE,
                'callback' => [$this, 'write_state'],
                'permission_callback' => [$this, 'can_write_state'],
                'args' => [
                    'state' => [
                        'required' => true,
                        'type' => 'object',
                    ],
                ],
            ],
        ]);
    }

    public function can_read_state() {
        return is_user_logged_in();
    }

    public function can_write_state() {
        return is_user_logged_in() && current_user_can('read');
    }

    public function read_state() {
        $state = get_user_meta(get_current_user_id(), self::META_KEY, true);
        if (!is_array($state)) {
            $state = $this->default_state();
        }
        return rest_ensure_response($state);
    }

    public function write_state(WP_REST_Request $request) {
        $state = $request->get_param('state');
        if (!is_array($state)) {
            return new WP_Error('hafte_invalid_state', 'Invalid workspace state.', ['status' => 400]);
        }

        $state = $this->sanitize_state($state);
        update_user_meta(get_current_user_id(), self::META_KEY, $state);
        return rest_ensure_response($state);
    }

    private function sanitize_state($state) {
        $clean = $this->default_state();
        foreach (['theme', 'activeView', 'newTask', 'noteDraft'] as $key) {
            if (isset($state[$key]) && is_string($state[$key])) {
                $clean[$key] = sanitize_text_field($state[$key]);
            }
        }
        if (isset($state['tasks']) && is_array($state['tasks'])) {
            $clean['tasks'] = array_values(array_map(function ($task) {
                return [
                    'id' => isset($task['id']) ? sanitize_key($task['id']) : wp_generate_uuid4(),
                    'title' => isset($task['title']) ? sanitize_text_field($task['title']) : '',
                    'due' => isset($task['due']) ? sanitize_text_field($task['due']) : 'امروز',
                    'priority' => isset($task['priority']) ? sanitize_text_field($task['priority']) : 'متوسط',
                    'project' => isset($task['project']) ? sanitize_text_field($task['project']) : 'بدون پروژه',
                    'done' => !empty($task['done']),
                ];
            }, array_slice($state['tasks'], 0, 500)));
        }
        if (isset($state['notes']) && is_array($state['notes'])) {
            $clean['notes'] = array_values(array_map(function ($note) {
                return [
                    'id' => isset($note['id']) ? sanitize_key($note['id']) : wp_generate_uuid4(),
                    'title' => isset($note['title']) ? sanitize_text_field($note['title']) : '',
                    'body' => isset($note['body']) ? sanitize_textarea_field($note['body']) : '',
                    'updated' => isset($note['updated']) ? sanitize_text_field($note['updated']) : '',
                ];
            }, array_slice($state['notes'], 0, 200)));
        }
        return $clean;
    }

    private function default_state() {
        return [
            'theme' => 'light',
            'activeView' => 'dashboard',
            'newTask' => '',
            'noteDraft' => '',
            'tasks' => [],
            'notes' => [],
        ];
    }
}

new Hafte_Plugin();
