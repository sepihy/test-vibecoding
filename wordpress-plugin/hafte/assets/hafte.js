(function () {
  'use strict';

  const cfg = window.HafteConfig || {};
  const root = document.querySelector('[data-hafte-app]');
  if (!root) return;

  const fa = (value) => String(value).replace(/\d/g, (d) => '۰۱۲۳۴۵۶۷۸۹'[d]);
  const esc = (value) => String(value == null ? '' : value)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
  const uid = () => 't-' + Date.now().toString(36) + Math.random().toString(36).slice(2, 7);
  const icon = (name) => {
    const paths = {
      grid: '<rect x="4" y="4" width="7" height="7"/><rect x="13" y="4" width="7" height="4"/><rect x="13" y="10" width="7" height="10"/><rect x="4" y="13" width="7" height="7"/>',
      inbox: '<path d="M4 13l2-8h12l2 8M4 13v6h16v-6M4 13h5l1 2h4l1-2h5"/>',
      tasks: '<path d="M9 6h11M9 12h11M9 18h11M3.5 6l1 1 2-2M3.5 12l1 1 2-2M3.5 18l1 1 2-2"/>',
      calendar: '<rect x="5" y="5" width="14" height="15"/><path d="M5 10h14M9 3v4M15 3v4"/>',
      planner: '<path d="M4 4h16v16H4zM9 4v16M15 4v16M4 9h5M15 14h5"/>',
      folder: '<path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>',
      target: '<circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="4"/>',
      repeat: '<path d="M17 2l3 3-3 3M3 11V9a4 4 0 0 1 4-4h13M7 22l-3-3 3-3M21 13v2a4 4 0 0 1-4 4H4"/>',
      clock: '<circle cx="12" cy="13" r="8"/><path d="M12 9v4l3 2M10 2h4"/>',
      note: '<path d="M6 3h9l4 4v14H6zM14 3v5h5M9 13h7M9 17h7"/>',
      chart: '<path d="M5 20V10M12 20V4M19 20v-7"/>',
      review: '<path d="M20 12a8 8 0 1 1-2.3-5.6M20 4v5h-5M9 12l2 2 4-4"/>',
      settings: '<path d="M4 7h10M18 7h2M4 17h2M10 17h10M14 5v4M6 15v4"/>',
      search: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
      sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
      moon: '<path d="M20 14a8 8 0 1 1-10-10 6 6 0 0 0 10 10z"/>',
      plus: '<path d="M12 5v14M5 12h14"/>',
      check: '<path d="M5 12l5 5 9-10"/>',
      menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
      panel: '<rect x="4" y="5" width="16" height="14"/><path d="M9 5v14"/>',
      trash: '<path d="M5 7h14M10 11v5M14 11v5M7 7l1 13h8l1-13M9 7l1-3h4l1 3"/>',
      arrow: '<path d="M5 12h14M13 6l6 6-6 6"/>'
    };
    return '<svg viewBox="0 0 24 24" aria-hidden="true">' + (paths[name] || paths.grid) + '</svg>';
  };

  const navGroups = [
    { title: 'روزانه', items: [['dashboard', 'داشبورد', 'grid'], ['inbox', 'صندوق ورودی', 'inbox'], ['tasks', 'کارها', 'tasks'], ['calendar', 'تقویم', 'calendar']] },
    { title: 'برنامه‌ریزی', items: [['planner', 'برنامه هفتگی', 'planner'], ['projects', 'پروژه‌ها', 'folder'], ['goals', 'اهداف', 'target'], ['habits', 'عادت‌ها', 'repeat']] },
    { title: 'تمرکز و ثبت', items: [['focus', 'تمرکز و زمان', 'clock'], ['notes', 'یادداشت‌ها', 'note']] },
    { title: 'مرور', items: [['reports', 'گزارش‌ها', 'chart'], ['review', 'مرور', 'review']] }
  ];
  const defaultState = {
    theme: 'light', activeView: 'dashboard', newTask: '', noteDraft: '', tasks: [
      { id: uid(), title: 'مرور برنامه هفتگی', due: 'امروز', priority: 'مهم', project: 'شخصی', done: false },
      { id: uid(), title: 'ارسال گزارش فصل دوم', due: 'فردا', priority: 'متوسط', project: 'کار', done: false },
      { id: uid(), title: 'خرید کتاب طراحی محصول', due: 'این هفته', priority: 'کم', project: 'بدون پروژه', done: true }
    ], notes: [
      { id: uid(), title: 'ایده‌های هفته', body: 'سه کار مهم را پیش از شروع روز مشخص کن.', updated: 'امروز' }
    ]
  };
  let state = JSON.parse(JSON.stringify(defaultState));
  let ui = { collapsed: false, quick: false, search: '', toast: '', due: 'امروز', priority: 'متوسط', project: 'بدون پروژه', timerSeconds: 25 * 60, timerRunning: false, timerId: null };

  function mergeState(saved) {
    if (!saved || typeof saved !== 'object') return;
    state = Object.assign(state, saved);
    state.tasks = Array.isArray(saved.tasks) ? saved.tasks : state.tasks;
    state.notes = Array.isArray(saved.notes) ? saved.notes : state.notes;
  }
  function persist() {
    const payload = JSON.stringify(state);
    try { localStorage.setItem(cfg.storageKey || 'hafte-workspace-v1', payload); } catch (e) {}
    if (cfg.loggedIn && cfg.apiUrl) {
      fetch(cfg.apiUrl, { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce }, body: JSON.stringify({ state }) }).catch(() => {});
    }
  }
  function load() {
    let local = null;
    try { local = JSON.parse(localStorage.getItem(cfg.storageKey || 'hafte-workspace-v1')); } catch (e) {}
    mergeState(local);
    if (cfg.loggedIn && cfg.apiUrl) {
      fetch(cfg.apiUrl, { headers: { 'X-WP-Nonce': cfg.nonce } }).then((res) => res.ok ? res.json() : null).then(mergeState).then(render).catch(render);
    } else render();
  }
  function setState(next) { state = Object.assign(state, next); persist(); render(); }
  function showToast(message) { ui.toast = message; render(); window.clearTimeout(showToast.timer); showToast.timer = window.setTimeout(() => { ui.toast = ''; render(); }, 3500); }
  function todayText() { return 'یکشنبه ۲۶ مهر ۱۴۰۵'; }
  function taskCount(done) { return state.tasks.filter((task) => done ? task.done : !task.done).length; }
  function taskRows(limit) {
    const query = ui.search.trim().toLowerCase();
    return state.tasks.filter((task) => !query || [task.title, task.project, task.priority].join(' ').toLowerCase().includes(query)).slice(0, limit || 50);
  }
  function taskRow(task) {
    return '<div class="hafte-list-item"><button class="hafte-check ' + (task.done ? 'done' : '') + '" data-action="toggle-task" data-id="' + esc(task.id) + '" aria-label="تغییر وضعیت">' + (task.done ? icon('check') : '') + '</button><span class="hafte-task-text ' + (task.done ? 'done' : '') + '">' + esc(task.title) + '</span><span class="hafte-meta">' + esc(task.due) + '</span><span class="hafte-pill ' + (task.priority === 'فوری' ? 'warning' : '') + '">' + esc(task.priority) + '</span></div>';
  }
  function header(title, description, action) {
    return '<div class="hafte-view-heading"><div><h1>' + title + '</h1><p>' + description + '</p></div><div class="hafte-heading-actions">' + (action || '') + '</div></div>';
  }
  function card(content, cls) { return '<section class="hafte-card ' + (cls || 'pad') + '">' + content + '</section>'; }
  function dashboard() {
    const open = taskCount(false), done = taskCount(true), progress = state.tasks.length ? Math.round(done / state.tasks.length * 100) : 0;
    return header('سلام، روزت روشن', 'یک نمای آرام برای اینکه بدانی امروز چه چیزی اهمیت دارد.', '<button class="hafte-btn primary" data-action="quick"><span>' + icon('plus') + '</span>افزودن سریع</button>') +
      '<div class="hafte-grid"><div class="hafte-card hafte-stat"><div class="hafte-stat-label">کارهای باز</div><div class="hafte-stat-value">' + fa(open) + '</div><div class="hafte-stat-note">در مسیر امروز</div></div><div class="hafte-card hafte-stat"><div class="hafte-stat-label">تکمیل‌شده</div><div class="hafte-stat-value">' + fa(done) + '</div><div class="hafte-stat-note">آفرین، ادامه بده</div></div><div class="hafte-card hafte-stat"><div class="hafte-stat-label">تمرکز امروز</div><div class="hafte-stat-value">۲۵<span class="hafte-meta"> دقیقه</span></div><div class="hafte-stat-note">هدف شخصی</div></div><div class="hafte-card hafte-stat"><div class="hafte-stat-label">پیشرفت هفته</div><div class="hafte-stat-value">' + fa(progress) + '<span class="hafte-meta">٪</span></div><div class="hafte-stat-note">نسبت به برنامه</div></div>' +
      card('<h2>کارهای امروز</h2><div class="hafte-list">' + (taskRows(4).map(taskRow).join('') || '<div class="hafte-empty">امروز کاری در صندوق نیست.</div>') + '</div><button class="hafte-btn ghost" data-view="tasks" style="margin-top:12px">مشاهده همه کارها ' + icon('arrow') + '</button>', 'hafte-card hafte-section') +
      card('<h2>پیشرفت این هفته</h2><div style="display:flex;justify-content:space-between;margin-bottom:8px"><span class="hafte-meta">' + fa(done) + ' از ' + fa(state.tasks.length || 0) + ' کار</span><strong>' + fa(progress) + '٪</strong></div><div class="hafte-progress"><span style="width:' + progress + '%"></span></div><div class="hafte-list" style="margin-top:18px"><div class="hafte-list-item"><span class="hafte-pill success">今日</span><span class="hafte-task-text">یک قدم کوچک، اما پیوسته</span></div><div class="hafte-list-item"><span class="hafte-pill">برنامه</span><span class="hafte-task-text">برای فردا سه اولویت تعیین کن</span></div></div>', 'hafte-card hafte-section') + '</div>';
  }
  function tasksView(mode) {
    const title = mode === 'inbox' ? 'صندوق ورودی' : 'کارها';
    const list = mode === 'inbox' ? state.tasks : taskRows();
    return header(title, mode === 'inbox' ? 'همه چیز را اینجا خالی کن؛ بعداً مرتبش می‌کنیم.' : 'کارهای روزانه را ساده، شفاف و قابل انجام نگه دار.', '<button class="hafte-btn primary" data-action="quick">' + icon('plus') + 'افزودن کار</button>') +
      '<div class="hafte-tabs"><button class="hafte-tab ' + (mode === 'inbox' ? 'is-active' : '') + '" data-view="inbox">همه ورودی‌ها</button><button class="hafte-tab ' + (mode === 'tasks' ? 'is-active' : '') + '" data-view="tasks">کارهای باز</button><button class="hafte-tab">تکمیل‌شده‌ها</button></div>' +
      card('<div class="hafte-list">' + (list.length ? list.map(taskRow).join('') : '<div class="hafte-empty">چیزی برای نمایش وجود ندارد.</div>') + '</div>', 'hafte-card pad');
  }
  function calendarView() {
    const days = ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنجشنبه', 'جمعه'];
    return header('تقویم', 'قرارها و کارهای زمان‌دار را در یک نگاه ببین.', '<button class="hafte-btn primary" data-action="quick">' + icon('plus') + 'رویداد جدید</button>') + card('<div class="hafte-tabs"><button class="hafte-tab is-active">این هفته</button><button class="hafte-tab">ماهانه</button></div><div class="hafte-table-wrap"><table class="hafte-table"><thead><tr><th class="slot">زمان</th>' + days.map((day, i) => '<th>' + day + '<br><span class="hafte-meta">' + fa(i + 26) + ' مهر</span></th>').join('') + '</tr></thead><tbody><tr><td class="slot">۹:۰۰</td>' + days.map((d, i) => '<td>' + (i === 1 ? '<div class="hafte-event">مرور برنامه هفتگی<br><small>۳۰ دقیقه</small></div>' : '') + '</td>').join('') + '</tr><tr><td class="slot">۱۱:۰۰</td>' + days.map((d, i) => '<td>' + (i === 3 ? '<div class="hafte-event">جلسه تیم محصول<br><small>آنلاین</small></div>' : '') + '</td>').join('') + '</tr><tr><td class="slot">۱۶:۳۰</td>' + days.map((d, i) => '<td>' + (i === 5 ? '<div class="hafte-event">مطالعه و تمرکز</div>' : '') + '</td>').join('') + '</tr></tbody></table></div>', 'hafte-card pad');
  }
  function plannerView() {
    const days = ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنجشنبه', 'جمعه'];
    return header('برنامه هفتگی', 'هفته را از بالا ببین و فضای خالی را آگاهانه نگه دار.', '<button class="hafte-btn">این هفته</button><button class="hafte-btn primary" data-action="quick">' + icon('plus') + 'افزودن</button>') + card('<div class="hafte-table-wrap"><table class="hafte-table"><thead><tr><th class="slot">روز</th>' + days.map((d) => '<th>' + d + '</th>').join('') + '</tr></thead><tbody><tr><td class="slot">اولویت اصلی</td>' + days.map((d, i) => '<td>' + (i < 3 ? '<div class="hafte-event">' + ['طراحی جریان ورود', 'ارسال گزارش', 'مرور اهداف'][i] + '</div>' : '<span class="hafte-meta">فضای باز</span>') + '</td>').join('') + '</tr><tr><td class="slot">تمرکز</td>' + days.map((d, i) => '<td>' + (i % 2 === 0 ? '<div class="hafte-event">۹۰ دقیقه تمرکز</div>' : '') + '</td>').join('') + '</tr></tbody></table></div>', 'hafte-card pad');
  }
  function projectsView() {
    const projects = [['پایان‌نامه', '۶ کار باز', '#3a47b5', 62], ['کار', '۳ کار باز', '#2f7a4b', 40], ['شخصی', '۲ کار باز', '#8f5a00', 78]];
    return header('پروژه‌ها', 'کارهای بزرگ را به مسیرهای قابل دیدن تبدیل کن.', '<button class="hafte-btn primary" data-action="quick">' + icon('plus') + 'پروژه جدید</button>') + '<div class="hafte-grid">' + projects.map((p) => card('<div style="display:flex;justify-content:space-between;align-items:center"><h2 style="margin:0">' + p[0] + '</h2><span class="hafte-pill">' + p[1] + '</span></div><div class="hafte-progress" style="margin-top:22px"><span style="width:' + p[3] + '%;background:' + p[2] + '"></span></div><div style="display:flex;justify-content:space-between;margin-top:10px"><span class="hafte-meta">پیشرفت</span><strong>' + fa(p[3]) + '٪</strong></div>', 'hafte-card hafte-section')).join('') + '</div>';
  }
  function goalsView() {
    return header('اهداف', 'جهت کلی را نگه دار؛ هر هفته فقط یک قدم نزدیک‌تر شو.', '<button class="hafte-btn primary" data-action="quick">' + icon('plus') + 'هدف جدید</button>') + '<div class="hafte-grid">' + [['سلامت و انرژی', 'سه جلسه ورزش در هفته', 66], ['یادگیری', 'تکمیل دوره طراحی محصول', 38], ['کار و اثرگذاری', 'انتشار نسخه اول محصول', 52]].map((g) => card('<h2>' + g[0] + '</h2><p class="hafte-meta">' + g[1] + '</p><div class="hafte-progress" style="margin-top:18px"><span style="width:' + g[2] + '%"></span></div><div style="display:flex;justify-content:space-between;margin-top:9px"><span class="hafte-meta">تا پایان فصل</span><strong>' + fa(g[2]) + '٪</strong></div>', 'hafte-card hafte-section')).join('') + '</div>';
  }
  function habitsView() {
    const habits = [['آب کافی', '۵ از ۷ روز', 71], ['مطالعه', '۴ از ۷ روز', 57], ['خواب منظم', '۶ از ۷ روز', 86]];
    return header('عادت‌ها', 'تکرارهای کوچک، سیستم بزرگ زندگی را می‌سازند.', '<button class="hafte-btn primary" data-action="quick">' + icon('plus') + 'عادت جدید</button>') + '<div class="hafte-grid">' + habits.map((h) => card('<div style="display:flex;justify-content:space-between;align-items:center"><h2 style="margin:0">' + h[0] + '</h2><button class="hafte-check done" aria-label="ثبت امروز">' + icon('check') + '</button></div><p class="hafte-meta">' + h[1] + '</p><div class="hafte-progress"><span style="width:' + h[2] + '%"></span></div>', 'hafte-card hafte-section')).join('') + '</div>';
  }
  function focusView() {
    const min = String(Math.floor(ui.timerSeconds / 60)).padStart(2, '0'), sec = String(ui.timerSeconds % 60).padStart(2, '0');
    return header('تمرکز و زمان', 'یک بازه را انتخاب کن و فقط با همین کار بمان.', '<span class="hafte-pill success">حالت آرام</span>') + card('<div class="hafte-timer"><div><div class="hafte-meta">تمرکز فعلی</div><div class="hafte-timer-face">' + fa(min + ':' + sec) + '</div><div class="hafte-timer-controls"><button class="hafte-btn primary" data-action="timer">' + (ui.timerRunning ? 'مکث' : 'شروع تمرکز') + '</button><button class="hafte-btn" data-action="reset-timer">بازنشانی</button></div></div></div>', 'hafte-card pad') + '<div style="height:16px"></div>' + card('<h2>جلسه‌های امروز</h2><div class="hafte-list"><div class="hafte-list-item"><span class="hafte-pill success">۲۵ دقیقه</span><span class="hafte-task-text">طراحی صفحه داشبورد</span><span class="hafte-meta">تکمیل شد</span></div></div>', 'hafte-card pad');
  }
  function notesView() {
    return header('یادداشت‌ها', 'فکرها را سریع ثبت کن و بعداً به آن‌ها برگرد.', '<button class="hafte-btn primary" data-action="new-note">' + icon('plus') + 'یادداشت جدید</button>') + '<div class="hafte-note-grid">' + (state.notes.length ? state.notes.map((note) => '<article class="hafte-card hafte-note"><h3>' + esc(note.title) + '</h3><p>' + esc(note.body) + '</p><div class="hafte-meta" style="margin-top:16px">' + esc(note.updated || 'همین حالا') + '</div></article>').join('') : '<div class="hafte-empty">هنوز یادداشتی ثبت نشده است.</div>') + '</div>';
  }
  function reportsView() {
    return header('گزارش‌ها', 'الگوهای هفته را ببین تا تصمیم‌های بهتری بگیری.', '<button class="hafte-btn">این هفته</button>') + '<div class="hafte-grid">' + card('<h2>نرخ تکمیل کارها</h2><div class="hafte-stat-value">۷۴٪</div><div class="hafte-progress" style="margin-top:16px"><span style="width:74%"></span></div><p class="hafte-meta">۱۲٪ بهتر از هفته گذشته</p>', 'hafte-card hafte-section') + card('<h2>تمرکز عمیق</h2><div class="hafte-stat-value">۴ساعت ۲۰دقیقه</div><div class="hafte-list" style="margin-top:14px"><div class="hafte-list-item"><span class="hafte-task-text">دوشنبه</span><strong>۱:۱۰</strong></div><div class="hafte-list-item"><span class="hafte-task-text">چهارشنبه</span><strong>۱:۴۵</strong></div><div class="hafte-list-item"><span class="hafte-task-text">جمعه</span><strong>۱:۲۵</strong></div></div>', 'hafte-card hafte-section') + '</div>';
  }
  function reviewView() {
    return header('مرور', 'با چند سؤال کوتاه هفته را جمع‌بندی کن.', '<span class="hafte-pill">مرور هفتگی</span>') + card('<h2>چه چیزی این هفته خوب پیش رفت؟</h2><textarea rows="4" placeholder="چند خط برای خودت بنویس…"></textarea><h2 style="margin-top:22px">هفته بعد چه چیزی مهم‌تر است؟</h2><textarea rows="4" placeholder="یک اولویت اصلی…"></textarea><div style="margin-top:14px"><button class="hafte-btn primary" data-action="save-review">ذخیره مرور</button></div>', 'hafte-card pad');
  }
  function settingsView() {
    const dark = state.theme === 'dark';
    return header('تنظیمات', 'فضای هفته را مطابق ریتم خودت تنظیم کن.', '') + card('<div class="hafte-setting-row"><div class="hafte-setting-label"><strong>پوسته تاریک</strong><span>برای کار در نور کم</span></div><button class="hafte-switch ' + (dark ? 'on' : '') + '" data-action="theme" aria-label="تغییر پوسته"></button></div><div class="hafte-setting-row"><div class="hafte-setting-label"><strong>ذخیره خودکار</strong><span>تغییرات شما در این مرورگر ذخیره می‌شود</span></div><button class="hafte-switch on" aria-label="ذخیره خودکار فعال"></button></div><div class="hafte-setting-row"><div class="hafte-setting-label"><strong>حساب کاربری</strong><span>' + (cfg.loggedIn ? 'همگام‌سازی WordPress فعال است' : 'برای همگام‌سازی بین دستگاه‌ها وارد حساب شوید') + '</span></div><span class="hafte-pill ' + (cfg.loggedIn ? 'success' : '') + '">' + (cfg.loggedIn ? 'متصل' : 'محلی') + '</span></div>', 'hafte-card pad');
  }
  function renderView() {
    switch (state.activeView) {
      case 'inbox': return tasksView('inbox');
      case 'tasks': return tasksView('tasks');
      case 'calendar': return calendarView();
      case 'planner': return plannerView();
      case 'projects': return projectsView();
      case 'goals': return goalsView();
      case 'habits': return habitsView();
      case 'focus': return focusView();
      case 'notes': return notesView();
      case 'reports': return reportsView();
      case 'review': return reviewView();
      case 'settings': return settingsView();
      default: return dashboard();
    }
  }
  function render() {
    root.classList.toggle('hafte-dark', state.theme === 'dark');
    const sidebar = navGroups.map((group) => '<div class="hafte-group">' + group.title + '</div>' + group.items.map((item) => '<button class="hafte-nav ' + (state.activeView === item[0] ? 'is-active' : '') + '" data-view="' + item[0] + '" title="' + item[1] + '">' + icon(item[2]) + '<span>' + item[1] + '</span>' + (item[0] === 'inbox' && taskCount(false) ? '<span class="hafte-count">' + fa(taskCount(false)) + '</span>' : '') + '</button>').join('')).join('');
    root.innerHTML = '<div class="hafte-shell"><aside class="hafte-sidebar"><div class="hafte-brand"><span class="hafte-mark">' + icon('planner') + '</span><span>هفته</span></div>' + sidebar + '<div class="hafte-sidebar-spacer"></div><button class="hafte-nav ' + (state.activeView === 'settings' ? 'is-active' : '') + '" data-view="settings" title="تنظیمات">' + icon('settings') + '<span>تنظیمات</span></button><button class="hafte-nav" data-action="collapse" title="جمع کردن منو">' + icon('panel') + '<span>جمع کردن منو</span></button></aside><main class="hafte-main"><header class="hafte-header"><label class="hafte-search">' + icon('search') + '<input data-search type="search" value="' + esc(ui.search) + '" placeholder="جست‌وجو در کارها، پروژه‌ها و یادداشت‌ها…" aria-label="جست‌وجو"></label><span class="hafte-pill warning">داده‌های نمونه — ذخیره می‌شوند</span><span class="hafte-toolbar-spacer"></span><span class="hafte-date">' + todayText() + '</span><button class="hafte-btn ghost icon" data-action="theme" aria-label="تغییر پوسته">' + icon(state.theme === 'dark' ? 'sun' : 'moon') + '</button><button class="hafte-btn primary" data-action="quick">' + icon('plus') + '<span>افزودن سریع</span></button></header><div class="hafte-content"><div class="hafte-view">' + renderView() + '</div></div></main></div>' + (ui.quick ? quickModal() : '') + (ui.toast ? '<div class="hafte-toast" role="status"><span>' + icon('check') + '</span><span>' + esc(ui.toast) + '</span><button data-action="undo">واگرد</button></div>' : '');
    if (ui.collapsed) root.querySelector('.hafte-sidebar').classList.add('is-collapsed');
  }
  function quickModal() {
    const options = (label, key, values) => '<div class="hafte-option-row"><span class="hafte-option-label">' + label + '</span>' + values.map((value) => '<button class="hafte-option ' + (ui[key] === value ? 'is-active' : '') + '" data-option="' + key + '" data-value="' + esc(value) + '">' + value + '</button>').join('') + '</div>';
    return '<div class="hafte-modal-backdrop" data-action="close-modal"><div class="hafte-modal" role="dialog" aria-modal="true" aria-label="افزودن سریع کار"><div class="hafte-modal-head"><label for="hafte-quick-input">چه کاری در ذهنت هست؟</label><input id="hafte-quick-input" data-quick-input autofocus placeholder="مثلاً: ارسال گزارش فصل دوم تا پنجشنبه"></div><div class="hafte-modal-body">' + options('موعد', 'due', ['امروز', 'فردا', 'این هفته', 'بدون موعد']) + options('اولویت', 'priority', ['فوری', 'مهم', 'متوسط', 'کم']) + options('پروژه', 'project', ['بدون پروژه', 'پایان‌نامه', 'کار', 'شخصی']) + '</div><div class="hafte-modal-foot"><span>فقط عنوان کافی است؛ جزئیات را بعداً تکمیل کن.</span><button class="hafte-btn ghost" data-action="close-modal">انصراف</button><button class="hafte-btn primary" data-action="add-task">افزودن به صندوق ورودی</button></div></div></div>';
  }
  function toggleTask(id) { state.tasks = state.tasks.map((task) => task.id === id ? Object.assign({}, task, { done: !task.done }) : task); persist(); render(); }
  function startTimer() {
    ui.timerRunning = !ui.timerRunning;
    if (ui.timerRunning) ui.timerId = window.setInterval(() => { ui.timerSeconds = Math.max(0, ui.timerSeconds - 1); if (!ui.timerSeconds) { ui.timerRunning = false; window.clearInterval(ui.timerId); showToast('جلسه تمرکز تمام شد'); } render(); }, 1000);
    else window.clearInterval(ui.timerId);
    render();
  }
  root.addEventListener('click', (event) => {
    const target = event.target.closest('[data-view],[data-action],[data-option]');
    if (!target) return;
    if (target.dataset.view) { state.activeView = target.dataset.view; persist(); render(); return; }
    const action = target.dataset.action;
    if (target.dataset.option) { ui[target.dataset.option] = target.dataset.value; render(); return; }
    if (action === 'quick') { ui.quick = true; render(); return; }
    if (action === 'close-modal' && target.classList.contains('hafte-modal-backdrop')) { ui.quick = false; render(); return; }
    if (action === 'close-modal') { ui.quick = false; render(); return; }
    if (action === 'collapse') { ui.collapsed = !ui.collapsed; render(); return; }
    if (action === 'theme') { state.theme = state.theme === 'dark' ? 'light' : 'dark'; persist(); render(); return; }
    if (action === 'toggle-task') { toggleTask(target.dataset.id); return; }
    if (action === 'add-task') {
      const input = root.querySelector('[data-quick-input]'), title = input && input.value.trim();
      if (!title) { if (input) input.focus(); return; }
      state.tasks.unshift({ id: uid(), title, due: ui.due, priority: ui.priority, project: ui.project, done: false }); state.activeView = 'inbox'; ui.quick = false; persist(); showToast('کار به صندوق ورودی اضافه شد'); return;
    }
    if (action === 'reset-timer') { ui.timerRunning = false; window.clearInterval(ui.timerId); ui.timerSeconds = 25 * 60; render(); return; }
    if (action === 'timer') { startTimer(); return; }
    if (action === 'new-note') { const title = window.prompt('عنوان یادداشت'); if (title) { state.notes.unshift({ id: uid(), title, body: 'یادداشت جدید را اینجا ادامه بده.', updated: 'همین حالا' }); persist(); render(); } return; }
    if (action === 'save-review') { showToast('مرور هفته ذخیره شد'); return; }
    if (action === 'undo') { showToast('واگرد در این نسخه نمایشی آماده است'); }
  });
  root.addEventListener('input', (event) => {
    if (event.target.matches('[data-search]')) { ui.search = event.target.value; if (state.activeView === 'dashboard' || state.activeView === 'tasks' || state.activeView === 'inbox') render(); }
  });
  document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && ui.quick) { ui.quick = false; render(); } });
  load();
})();
