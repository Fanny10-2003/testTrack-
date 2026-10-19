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
