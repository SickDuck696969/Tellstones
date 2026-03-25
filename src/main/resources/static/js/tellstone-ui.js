document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-open-modal]").forEach((trigger) => {
        trigger.addEventListener("click", () => {
            const modal = document.getElementById(trigger.dataset.openModal);
            if (modal) {
                modal.classList.add("is-open");
            }
        });
    });

    document.querySelectorAll("[data-close-modal]").forEach((trigger) => {
        trigger.addEventListener("click", () => {
            const modal = trigger.closest(".modal-backdrop");
            if (modal) {
                modal.classList.remove("is-open");
            }
        });
    });

    document.querySelectorAll(".modal-backdrop").forEach((modal) => {
        modal.addEventListener("click", (event) => {
            if (event.target === modal) {
                modal.classList.remove("is-open");
            }
        });
    });

    const bgm = document.querySelector("[data-bgm]");
    const volumeSlider = document.querySelector("[data-bgm-volume]");
    const storedVolume = localStorage.getItem("tellstones-bgm-volume");

    if (bgm && volumeSlider) {
        const initialVolume = storedVolume !== null ? Number(storedVolume) : 0.35;
        bgm.volume = initialVolume;
        volumeSlider.value = initialVolume;

        volumeSlider.addEventListener("input", () => {
            bgm.volume = Number(volumeSlider.value);
            localStorage.setItem("tellstones-bgm-volume", volumeSlider.value);
        });

        bgm.play().catch(() => {
        });
    }

    const roomIdLabel = document.querySelector("[data-room-id]");
    const copyButton = document.querySelector("[data-copy-room]");
    const joinButton = document.querySelector("[data-join-room]");
    const joinInput = document.querySelector("[data-join-input]");
    const roomBanner = document.querySelector("[data-room-banner]");
    const roomStatus = document.querySelector("[data-room-status]");

    if (roomIdLabel && copyButton && joinButton && joinInput && window.io) {
        const socket = io("http://localhost:9092", { transports: ["websocket"] });

        socket.on("connect", () => {
            const roomId = generateRoomId();
            roomIdLabel.textContent = roomId;
            if (roomStatus) {
                roomStatus.textContent = "Lanterns lit. Your chamber is ready.";
            }
            socket.emit("startRoom", roomId);
        });

        socket.on("roomJoined", (roomId) => {
            if (roomBanner) {
                roomBanner.textContent = "Joined chamber " + roomId.toUpperCase();
            }
        });

        copyButton.addEventListener("click", async () => {
            try {
                await navigator.clipboard.writeText(roomIdLabel.textContent || "");
                copyButton.textContent = "Copied";
                setTimeout(() => {
                    copyButton.textContent = "Copy Sigil";
                }, 1200);
            } catch (error) {
                copyButton.textContent = "Copy Failed";
            }
        });

        joinButton.addEventListener("click", () => {
            const roomId = joinInput.value.trim();
            if (!roomId) {
                return;
            }
            socket.emit("inRoom", roomId);
            if (roomBanner) {
                roomBanner.textContent = "Seeking chamber " + roomId.toUpperCase();
            }
        });
    }

    const ruleCanvas = document.querySelector("[data-rule-canvas]");
    const pageLabel = document.querySelector("[data-rule-page]");
    const prevButtons = document.querySelectorAll("[data-rule-prev]");
    const nextButtons = document.querySelectorAll("[data-rule-next]");
    const loadingLabel = document.querySelector("[data-rule-loading]");

    if (ruleCanvas && pageLabel && prevButtons.length && nextButtons.length && window.pdfjsLib) {
        pdfjsLib.GlobalWorkerOptions.workerSrc = "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.4.456/pdf.worker.min.js";

        const context = ruleCanvas.getContext("2d");
        let pdfDoc = null;
        let page = 1;
        let rendering = false;

        const render = async () => {
            if (!pdfDoc || rendering) {
                return;
            }

            rendering = true;
            if (loadingLabel) {
                loadingLabel.textContent = "Loading page " + page + "...";
            }

            const pdfPage = await pdfDoc.getPage(page);
            const shell = ruleCanvas.parentElement;
            const unscaled = pdfPage.getViewport({ scale: 1 });
            const maxWidth = shell.clientWidth - 32;
            const maxHeight = shell.clientHeight - 32;
            const scale = Math.min(maxWidth / unscaled.width, maxHeight / unscaled.height);
            const viewport = pdfPage.getViewport({ scale });

            ruleCanvas.width = viewport.width;
            ruleCanvas.height = viewport.height;

            await pdfPage.render({
                canvasContext: context,
                viewport
            }).promise;

            pageLabel.textContent = "Page " + page + " / " + pdfDoc.numPages;
            if (loadingLabel) {
                loadingLabel.textContent = "";
            }
            rendering = false;
        };

        pdfjsLib.getDocument("/rules/Rule.pdf").promise.then((doc) => {
            pdfDoc = doc;
            render();
        }).catch(() => {
            if (loadingLabel) {
                loadingLabel.textContent = "Unable to load the rulebook.";
            }
        });

        prevButtons.forEach((button) => {
            button.addEventListener("click", () => {
                if (pdfDoc && page > 1) {
                    page -= 1;
                    render();
                }
            });
        });

        nextButtons.forEach((button) => {
            button.addEventListener("click", () => {
                if (pdfDoc && page < pdfDoc.numPages) {
                    page += 1;
                    render();
                }
            });
        });

        window.addEventListener("resize", () => {
            if (pdfDoc) {
                render();
            }
        });
    }

    const paymentForm = document.querySelector("[data-payment-form]");
    const paymentModal = document.getElementById("payment-confirmation");

    if (paymentForm && paymentModal) {
        paymentForm.addEventListener("submit", (event) => {
            event.preventDefault();
            paymentModal.classList.add("is-open");
        });
    }
});

function generateRoomId() {
    return Math.random().toString(36).slice(2, 8);
}
