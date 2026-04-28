"use strict";

const api = {
    async getAllResults() {
        const res = await fetch("/api/all");
        if (!res.ok) throw new Error(`Impossible de charger l'historique (HTTP ${res.status})`);
        return res.json();
    },

    async registerSample(data) {
        const res = await fetch("/api/register", {
            method:  "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(data),
        });
        if (!res.ok) throw new Error(`Enregistrement échoué (HTTP ${res.status})`);
        return res.json();
    },

    async getResult(id) {
        const res = await fetch(`/api/results/${id}`);
        if (!res.ok) throw new Error(`Résultat introuvable (HTTP ${res.status})`);
        return res.json();
    },
};


const $ = (id) => document.getElementById(id);

function setStatus(message, type = "info") {
    const bar = $("status-bar");
    $("status-message").textContent = message;
    bar.className = type === "error" ? "status-error" : "";
    bar.classList.remove("hidden");
}

function clearStatus() {
    $("status-bar").classList.add("hidden");
}

function showSection(id) {
    ["section-registration", "section-registered", "section-result"].forEach((s) => {
        $(s).classList.toggle("hidden", s !== id);
    });
}

function advanceStepper(activeStep) {
    [1, 2, 3].forEach((n) => {
        const el = $(`step-${n}`);
        el.classList.remove("active", "done");
        if (n < activeStep)  el.classList.add("done");
        if (n === activeStep) el.classList.add("active");
    });
    document.querySelectorAll(".step-line").forEach((line, i) => {
        line.classList.toggle("done", i + 1 < activeStep);
    });
}


let currentSampleId = null;


$("form-sample").addEventListener("submit", async (e) => {
    e.preventDefault();

    const data = {
        patient:    $("patient").value.trim(),
        testType:   $("testType").value,
        sampleType: $("sampleType").value,
    };

    setStatus("Enregistrement et analyse en cours, veuillez patienter…");

    try {
        const sample = await api.registerSample(data);
        currentSampleId = sample.id;

        $("info-id").textContent      = sample.id;
        $("info-patient").textContent = sample.patient;
        $("info-test").textContent    = sample.testType;
        $("info-sample").textContent  = sample.sampleType;

        clearStatus();
        advanceStepper(2);
        showSection("section-registered");
    } catch (err) {
        setStatus(err.message, "error");
    }
});

$("btn-analyze").addEventListener("click", async () => {
    $("btn-analyze").disabled = true;
    setStatus("Récupération du résultat…");

    try {
        const result = await api.getResult(currentSampleId);

        $("result-value").textContent     = result.value          ?? "—";
        $("result-unit").textContent      = result.unit           ?? "—";
        $("result-patient").textContent   = result.patient        ?? "—";
        $("result-test").textContent      = result.testType       ?? "—";
        $("result-signature").textContent = result.signature      ?? "Non validé";

        const interp   = result.interpretation ?? "INCONNU";
        const interpEl = $("result-interpretation");
        const badgeEl  = $("result-interpretation-badge");
        interpEl.textContent  = interp;
        badgeEl.dataset.level = interp.toLowerCase();

        clearStatus();
        advanceStepper(3);
        showSection("section-result");
    } catch (err) {
        setStatus(err.message, "error");
        $("btn-analyze").disabled = false;
    }
});

$("btn-history").addEventListener("click", async () => {
    const section = $("section-history");
    const isVisible = !section.classList.contains("hidden");

    if (isVisible) {
        section.classList.add("hidden");
        $("btn-history").textContent = "Historique";
        return;
    }

    $("btn-history").textContent = "…";
    try {
        const results = await api.getAllResults();
        const body = $("history-body");
        body.innerHTML = "";

        if (results.length === 0) {
            $("history-empty").classList.remove("hidden");
            $("history-table").classList.add("hidden");
        } else {
            $("history-empty").classList.add("hidden");
            $("history-table").classList.remove("hidden");
            results.forEach((r) => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${r.patient ?? "—"}</td>
                    <td>${r.testType ?? "—"}</td>
                    <td>${r.sampleType ?? "—"}</td>
                    <td>${r.value ?? "—"} ${r.unit ?? ""}</td>
                    <td><span class="result-badge" data-level="${(r.interpretation ?? "").toLowerCase()}">${r.interpretation ?? "—"}</span></td>
                    <td><code>${r.signature ?? "—"}</code></td>
                `;
                body.appendChild(tr);
            });
        }

        section.classList.remove("hidden");
        section.scrollIntoView({ behavior: "smooth", block: "nearest" });
    } catch (err) {
        setStatus(err.message, "error");
    } finally {
        $("btn-history").textContent = "Historique";
    }
});

$("btn-reset").addEventListener("click", () => {
    currentSampleId = null;
    $("form-sample").reset();
    $("btn-analyze").disabled = false;
    clearStatus();
    advanceStepper(1);
    showSection("section-registration");
});
