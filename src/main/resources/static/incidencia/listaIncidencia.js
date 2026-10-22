const rows = Array.from(document.querySelectorAll('tbody tr[data-status]'));
const filterButtons = document.querySelectorAll('[data-filter]');
const searchInput = document.getElementById('search');
const noResults = document.getElementById('no-results');
let activeFilter = 'all';

function updateIncidencias() {
    const query = searchInput.value.trim().toLocaleLowerCase('es');
    let visibleCount = 0;

    rows.forEach((row) => {
        const matchesFilter = activeFilter === 'all' || row.dataset.status === activeFilter;
        const matchesSearch = `${row.dataset.code} ${row.dataset.title}`.toLocaleLowerCase('es').includes(query);
        const visible = matchesFilter && matchesSearch;
        row.hidden = !visible;
        if (visible) visibleCount++;
    });

    noResults.hidden = visibleCount !== 0 || rows.length === 0;
}

filterButtons.forEach((button) => {
    button.addEventListener('click', () => {
        activeFilter = button.dataset.filter;
        filterButtons.forEach((item) => {
            const isActive = item === button;
            item.classList.toggle('active', isActive);
            item.setAttribute('aria-pressed', String(isActive));
        });
        updateIncidencias();
    });
});
searchInput.addEventListener('input', updateIncidencias);

document.getElementById('critical-count').textContent =
    rows.filter((row) => row.dataset.severity === 'CRITICA').length;
document.getElementById('high-count').textContent =
    rows.filter((row) => row.dataset.severity === 'ALTA').length;
document.getElementById('closed-count').textContent =
    rows.filter((row) => row.dataset.status === 'CERRADO').length;
document.getElementById('total-count').textContent = rows.length;

function actualizarEstadoFila(row, nuevoEstado) {
    row.dataset.status = nuevoEstado;
    const status = row.querySelector('.status');
    status.textContent = nuevoEstado;
    status.classList.remove('status-abierto', 'status-cerrado', 'status-progreso');
    status.classList.add(nuevoEstado === 'ABIERTO'
        ? 'status-abierto'
        : nuevoEstado === 'CERRADO' ? 'status-cerrado' : 'status-progreso');
    document.getElementById('closed-count').textContent =
        rows.filter((item) => item.dataset.status === 'CERRADO').length;
    updateIncidencias();
}

rows.forEach((row) => {
    const dateElement = row.querySelector('[data-date]');
    if (dateElement.dataset.date) {
        const date = new Date(dateElement.dataset.date);
        dateElement.textContent = new Intl.DateTimeFormat('es-ES', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        }).format(date);
    }
});

updateIncidencias();

window.asignarIncidencia = async function (select) {
    const valorAnterior = select.dataset.original;
    const feedback = select.parentElement.nextElementSibling;
    const estabaDeshabilitado = select.disabled;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const body = new URLSearchParams({ usuarioId: select.value });
    select.disabled = true;
    feedback.textContent = 'Guardando…';
    feedback.classList.remove('assignment-error', 'assignment-success');

    try {
        const response = await fetch(select.dataset.url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                [csrfHeader]: csrfToken
            },
            body
        });
        if (!response.ok) {
            const detail = await response.json().catch(() => null);
            throw new Error(detail?.error || `No se pudo guardar la asignación (${response.status}).`);
        }
        const result = await response.json();
        select.dataset.original = select.value;
        const row = select.closest('tr');
        if (row.dataset.status !== result.estado) {
            actualizarEstadoFila(row, result.estado);
        }
        select.classList.add('assignment-saved');
        feedback.textContent = 'Asignación guardada';
        feedback.classList.add('assignment-success');
        window.setTimeout(() => select.classList.remove('assignment-saved'), 1200);
    } catch (error) {
        select.value = valorAnterior;
        feedback.textContent = error.message || 'No se pudo guardar la asignación.';
        feedback.classList.add('assignment-error');
    } finally {
        select.disabled = estabaDeshabilitado;
    }
};

window.cambiarEstadoIncidencia = async function (select) {
    const valorAnterior = select.dataset.original;
    const feedback = select.parentElement.parentElement.querySelector('.assignment-feedback');
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const params = new URLSearchParams({
        estado: select.value,
        proyectoId: select.dataset.projectId
    });
    select.disabled = true;
    feedback.textContent = 'Guardando…';
    feedback.classList.remove('assignment-error', 'assignment-success');

    try {
        const response = await fetch(`${select.dataset.url}?${params}`, {
            method: 'PATCH',
            headers: { [csrfHeader]: csrfToken }
        });
        if (!response.ok) {
            const detail = await response.text();
            throw new Error(detail || `No se pudo cambiar el estado (${response.status}).`);
        }
        const result = await response.json();
        select.dataset.original = result.estado;
        select.value = result.estado;
        actualizarEstadoFila(select.closest('tr'), result.estado);
        select.classList.add('assignment-saved');
        feedback.textContent = 'Estado guardado';
        feedback.classList.add('assignment-success');
        window.setTimeout(() => select.classList.remove('assignment-saved'), 1200);
    } catch (error) {
        select.value = valorAnterior;
        feedback.textContent = error.message || 'No se pudo cambiar el estado.';
        feedback.classList.add('assignment-error');
    } finally {
        select.disabled = false;
    }
};
