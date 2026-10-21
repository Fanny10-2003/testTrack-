(() => {
    const modal = document.getElementById("avatar-picker");
    const openButton = document.getElementById("open-avatar-picker");
    const closeButton = document.getElementById("close-avatar-picker");
    const cancelButton = document.getElementById("cancel-avatar-picker");
    const useButton = document.getElementById("use-avatar");
    const currentPreview = document.getElementById("current-avatar-preview");
    const currentName = document.getElementById("current-avatar-name");
    const photoInput = document.getElementById("fotoPerfil");
    const radioButtons = Array.from(document.querySelectorAll('input[name="avatarChoice"]'));

    if (!modal || !openButton || !closeButton || !cancelButton
        || !useButton || !currentName || !currentPreview || !photoInput) {
        return;
    }

    let confirmedValue = photoInput.value;
    let previousFocus = null;

    const selectedRadio = () => radioButtons.find((radio) => radio.checked);

    const selectRadioByValue = (value) => {
        radioButtons.forEach((radio) => {
            radio.checked = radio.value === value;
        });
    };

    const updateCurrentPreview = (selected) => {
        const image = selected.closest(".avatar-option").querySelector("img");
        const previewImage = document.createElement("img");
        previewImage.id = "current-avatar-image";
        previewImage.src = image.src;
        previewImage.alt = "";
        currentPreview.replaceChildren(previewImage);
        currentName.textContent = selected.closest(".avatar-option")
            .querySelector(".avatar-option-name").textContent;
    };

    const closeModal = (restoreSelection) => {
        if (restoreSelection) {
            selectRadioByValue(confirmedValue);
        } else {
            const selected = selectedRadio();
            if (selected) {
                photoInput.value = selected.value;
                confirmedValue = selected.value;
                updateCurrentPreview(selected);
            }
        }

        modal.hidden = true;
        document.body.classList.remove("avatar-modal-open");
        previousFocus?.focus();
    };

    openButton.addEventListener("click", () => {
        previousFocus = document.activeElement;
        confirmedValue = photoInput.value;
        selectRadioByValue(confirmedValue);
        modal.hidden = false;
        document.body.classList.add("avatar-modal-open");
        (selectedRadio() ?? radioButtons[0])?.focus();
    });

    closeButton.addEventListener("click", () => closeModal(true));
    cancelButton.addEventListener("click", () => closeModal(true));
    useButton.addEventListener("click", () => closeModal(false));

    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal(true);
        }
    });

    modal.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            event.preventDefault();
            closeModal(true);
        }
    });
})();
