const state = {
  entity: 'coaches',
  search: '',
  data: { coaches: [], athletes: [], programs: [], exercises: [], workouts: [] },
  refs: { coaches: [], athletes: [], programs: [], exercises: [] },
  page: 0,
  pageSize: 10,
};

const entities = {
  coaches: {
    title: 'Тренеры',
    endpoint: '/api/coaches',
    columns: [
      ['firstName', 'Имя'], ['lastName', 'Фамилия'], ['athletesCount', 'Спортсменов'],
    ],
    form: [
      { key: 'firstName', label: 'Имя', type: 'text', required: true },
      { key: 'lastName', label: 'Фамилия', type: 'text', required: true },
    ],
    payload: (v) => ({ firstName: v.firstName, lastName: v.lastName }),
  },
  athletes: {
    title: 'Спортсмены',
    endpoint: '/api/athletes',
    columns: [
      ['firstName', 'Имя'], ['lastName', 'Фамилия'], ['coachName', 'Тренер'],
    ],
    form: [
      { key: 'firstName', label: 'Имя', type: 'text', required: true },
      { key: 'lastName', label: 'Фамилия', type: 'text', required: true },
      { key: 'coachId', label: 'Тренер', type: 'select', ref: 'coaches' },
    ],
    payload: (v) => ({ firstName: v.firstName, lastName: v.lastName, coachId: v.coachId ? Number(v.coachId) : null }),
  },
  programs: {
    title: 'Программы тренировок',
    endpoint: '/api/programs',
    columns: [['name', 'Название'], ['workoutsCount', 'Тренировок']],
    form: [{ key: 'name', label: 'Название', type: 'text', required: true }],
    payload: (v) => ({ name: v.name }),
  },
  exercises: {
    title: 'Упражнения',
    endpoint: '/api/exercises',
    columns: [['name', 'Название']],
    form: [{ key: 'name', label: 'Название', type: 'text', required: true }],
    payload: (v) => ({ name: v.name }),
  },
  workouts: {
    title: 'Тренировки',
    endpoint: '/api/workouts',
    columns: [
      ['title', 'Название'], ['type', 'Тип'], ['durationMinutes', 'Длительность'],
      ['scheduledAt', 'Дата'], ['athleteName', 'Спортсмен'], ['programName', 'Программа'], ['exercisesCount', 'Упражнений'],
    ],
    form: [
      { key: 'title', label: 'Название', type: 'text', required: true },
      { key: 'type', label: 'Тип', type: 'text', required: true },
      { key: 'durationMinutes', label: 'Длительность (мин)', type: 'number', required: true },
      { key: 'scheduledAt', label: 'Дата и время', type: 'datetime-local', required: true },
      { key: 'athleteId', label: 'Спортсмен', type: 'select', ref: 'athletes', required: true },
      { key: 'programId', label: 'Программа', type: 'select', ref: 'programs', required: true },
      { key: 'exerciseIds', label: 'Упражнения', type: 'multiselect', ref: 'exercises', required: true },
    ],
    payload: (v) => ({
      title: v.title,
      type: v.type,
      durationMinutes: Number(v.durationMinutes),
      scheduledAt: normalizeDateTime(v.scheduledAt),
      athleteId: Number(v.athleteId),
      programId: Number(v.programId),
      exerciseIds: v.exerciseIds.map(Number),
    }),
  },
};

function normalizeDateTime(value) {
  if (!value) return value;
  return value.length === 16 ? `${value}:00` : value;
}

function toast(message) {
  const root = document.getElementById('toast-root');
  const item = document.createElement('div');
  item.className = 'toast';
  item.textContent = message;
  root.appendChild(item);
  setTimeout(() => item.remove(), 2800);
}

async function request(url, options = {}) {
  const response = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (response.status === 204) return null;
  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;
  if (!response.ok) throw new Error(payload?.message || payload?.error || `Ошибка ${response.status}`);
  return payload;
}

function fullName(item) { return item ? `${item.firstName} ${item.lastName}` : '—'; }

function optionLabel(ref, item) {
  if (ref === 'coaches' || ref === 'athletes') return fullName(item);
  return item.name;
}

async function loadRefs() {
  const [coaches, athletes, programs, exercises] = await Promise.all([
    request('/api/coaches'), request('/api/athletes'), request('/api/programs'), request('/api/exercises'),
  ]);
  state.refs = { coaches, athletes, programs, exercises };
}

async function loadEntity(entity) {
  const cfg = entities[entity];
  const data = await request(cfg.endpoint);
  state.data[entity] = data || [];
}

function filteredRows(entity) {
  const q = state.search.trim().toLowerCase();
  if (!q) return state.data[entity];
  return state.data[entity].filter((row) => Object.values(row).some((v) => String(v ?? '').toLowerCase().includes(q)));
}

function pagedRows(items) {
  const start = state.page * state.pageSize;
  return items.slice(start, start + state.pageSize);
}

function pagesCount(items) {
  return Math.max(1, Math.ceil(items.length / state.pageSize));
}

function renderSidebar() {
  const sidebar = document.getElementById('sidebar');
  sidebar.innerHTML = Object.entries(entities).map(([key, cfg]) =>
    `<button class="nav-btn ${key === state.entity ? 'active' : ''}" data-nav="${key}">${cfg.title}</button>`).join('');
  sidebar.querySelectorAll('[data-nav]').forEach((btn) => btn.addEventListener('click', async () => {
    state.entity = btn.dataset.nav;
    state.search = '';
    state.page = 0;
    document.getElementById('search-input').value = '';
    await refresh();
  }));
}

function renderTable() {
  const cfg = entities[state.entity];
  const allRows = filteredRows(state.entity);
  const totalPages = pagesCount(allRows);
  if (state.page >= totalPages) state.page = totalPages - 1;
  const rows = pagedRows(allRows);
  const title = document.getElementById('section-title');
  title.textContent = cfg.title;

  const head = cfg.columns.map(([, label]) => `<th>${label}</th>`).join('');
  const body = rows.map((row) => {
    const cells = cfg.columns.map(([key]) => `<td>${formatCell(state.entity, key, row[key])}</td>`).join('');
    return `<tr>${cells}<td class="actions"><button class="btn" data-edit="${row.id}">Изменить</button><button class="btn btn-danger" data-delete="${row.id}">Удалить</button></td></tr>`;
  }).join('');

  document.getElementById('table-wrap').innerHTML = `
    <table>
      <thead><tr>${head}<th>Действия</th></tr></thead>
      <tbody>${body || `<tr><td colspan="${cfg.columns.length + 1}" class="muted">Нет данных</td></tr>`}</tbody>
    </table>
    <div class="pager">
      <span class="pager-info">Страница ${state.page + 1} из ${totalPages}. Всего записей: ${allRows.length}</span>
      <div>
        <button class="btn" id="prev-page" ${state.page <= 0 ? "disabled" : ""}>Назад</button>
        <button class="btn" id="next-page" ${state.page + 1 >= totalPages ? "disabled" : ""}>Вперёд</button>
      </div>
    </div>`;

  document.querySelectorAll('[data-edit]').forEach((el) => el.addEventListener('click', () => openForm(Number(el.dataset.edit))));
  document.querySelectorAll('[data-delete]').forEach((el) => el.addEventListener('click', () => removeItem(Number(el.dataset.delete))));

  document.getElementById('prev-page')?.addEventListener('click', () => {
    if (state.page > 0) {
      state.page -= 1;
      renderTable();
    }
  });
  document.getElementById('next-page')?.addEventListener('click', () => {
    if (state.page + 1 < totalPages) {
      state.page += 1;
      renderTable();
    }
  });
}

function formatCell(entity, key, value) {
  if (entity === 'workouts' && key === 'scheduledAt' && value) return new Date(value).toLocaleString('ru-RU');
  return value ?? '—';
}

function openForm(id = null) {
  const cfg = entities[state.entity];
  const row = id ? state.data[state.entity].find((x) => x.id === id) : null;
  const title = row ? 'Редактирование' : 'Создание';

  const fields = cfg.form.map((f) => renderField(f, row)).join('');
  const root = document.getElementById('modal-root');
  root.innerHTML = `<div class="modal"><div class="modal-card"><h3>${title}: ${cfg.title}</h3><form id="entity-form"><div class="grid">${fields}</div><div class="row"><button class="btn btn-primary" type="submit">Сохранить</button><button class="btn" type="button" id="cancel-btn">Отмена</button></div></form></div></div>`;

  document.getElementById('cancel-btn').onclick = closeModal;
  document.getElementById('entity-form').onsubmit = async (e) => {
    e.preventDefault();
    try {
      const form = new FormData(e.target);
      const values = formToObject(form, cfg.form);
      const payload = cfg.payload(values);
      if (id) await request(`${cfg.endpoint}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await request(cfg.endpoint, { method: 'POST', body: JSON.stringify(payload) });
      closeModal();
      await refresh();
      toast('Сохранено');
    } catch (err) {
      toast(err.message);
    }
  };
}

function renderField(field, row) {
  const value = row?.[field.key] ?? '';
  if (field.type === 'select') {
    const opts = state.refs[field.ref] || [];
    return `<label>${field.label}<select class="input" name="${field.key}" ${field.required ? 'required' : ''}><option value="">Выберите...</option>${opts.map((o) => `<option value="${o.id}" ${String(value) === String(o.id) ? 'selected' : ''}>${optionLabel(field.ref, o)}</option>`).join('')}</select></label>`;
  }
  if (field.type === 'multiselect') {
    const opts = state.refs[field.ref] || [];
    return `<label>${field.label}<div class="checkbox-list">${opts.map((o) => `<label class="inline"><input type="checkbox" name="${field.key}" value="${o.id}"/> ${optionLabel(field.ref, o)}</label>`).join('')}</div></label>`;
  }
  return `<label>${field.label}<input class="input" name="${field.key}" type="${field.type}" value="${value ?? ''}" ${field.required ? 'required' : ''}/></label>`;
}

function formToObject(formData, fields) {
  const obj = {};
  for (const field of fields) {
    if (field.type === 'multiselect') {
      obj[field.key] = formData.getAll(field.key);
    } else {
      obj[field.key] = formData.get(field.key);
    }
  }
  return obj;
}

async function removeItem(id) {
  if (!confirm('Точно удалить запись?')) return;
  try {
    const cfg = entities[state.entity];
    await request(`${cfg.endpoint}/${id}`, { method: 'DELETE' });
    await refresh();
    toast('Удалено');
  } catch (err) {
    toast(err.message);
  }
}

function closeModal() { document.getElementById('modal-root').innerHTML = ''; }

async function refresh() {
  await loadRefs();
  await loadEntity(state.entity);
  renderSidebar();
  renderTable();
}

document.getElementById('search-input').addEventListener('input', (e) => {
  state.search = e.target.value;
  state.page = 0;
  renderTable();
});

document.getElementById('create-btn').addEventListener('click', () => openForm());

refresh().catch((e) => toast(e.message));