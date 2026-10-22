document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("conversation-form");
    const list = document.getElementById("conversation-list");
    const input = document.getElementById("conversation-content");
    const feedback = document.getElementById("conversation-feedback");
    const section = document.querySelector(".conversation-section");
    if (!form || !list || !input || !feedback || !section) return;

    const incidenciaId = section.dataset.incidenciaId;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const contenido = input.value.trim();
        if (!contenido) {
            feedback.textContent = "Escribe un comentario antes de enviarlo.";
            feedback.classList.add("is-error");
            return;
        }

        feedback.textContent = "Enviando...";
        feedback.classList.remove("is-error");
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        try {
            const response = await fetch(`/api/incidencias/${incidenciaId}/comentarios`, {
                method: "POST",
                headers,
                body: JSON.stringify({ contenido })
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.error || "No se pudo publicar el comentario.");

            document.getElementById("conversation-empty")?.remove();
            const entry = document.createElement("li");
            entry.className = "conversation-entry";
            const meta = document.createElement("div");
            meta.className = "conversation-meta";
            const author = document.createElement("strong");
            author.textContent = result.usuarioNombre;
            const time = document.createElement("time");
            time.dateTime = result.fechaCreacion;
            time.textContent = new Intl.DateTimeFormat("es", {
                dateStyle: "short",
                timeStyle: "short"
            }).format(new Date(result.fechaCreacion));
            const message = document.createElement("p");
            message.className = "preserve-lines";
            message.textContent = result.contenido;
            meta.append(author, time);
            entry.append(meta, message);
            list.append(entry);
            entry.scrollIntoView({ block: "nearest" });
            input.value = "";
            feedback.textContent = "Comentario publicado.";
        } catch (error) {
            feedback.textContent = error.message || "No se pudo publicar el comentario.";
            feedback.classList.add("is-error");
        }
    });
});
