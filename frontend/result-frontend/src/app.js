"use strict";

const config = window.LABOTRACK_CONFIG ?? { mockMode: true };


// ─── Client API réel ──────────────────────────────────────────────────────────
// nginx proxy /api/samples  → sample-api:9000
// nginx proxy /api/analyze  → analysis-api:9001
const api = {
    async registerSample(data) {
        const res = await fetch("/api/samples", {
            method:  "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(data),
        });
        if (!res.ok) throw new Error(`Enregistrement échoué (HTTP ${res.status})`);
        return res.json();
    },

    async analyze(id) {
        const res = await fetch(`/api/analyze/${id}`, { method: "POST" });
        if (!res.ok) throw new Error(`Analyse échouée (HTTP ${res.status})`);
        return res.json();
    },
};


// ─── Client mock ──────────────────────────────────────────────────────────────
const MOCK_RESULTS = {
    GLYCEMIE:        { value: "0.86", unit: "g/L",  interpretation: "NORMAL"   },
    NFS:             { value: "4.5",  unit: "T/L",  interpretation: "NORMAL"   },
    BILAN_HEPATIQUE: { value: "42",   unit: "UI/L", interpretation: "ELEVE"    },
    BILAN_RENAL:     { value: "7.2",  unit: "mg/L", interpretation: "CRITIQUE" },
};

const mockStore = {};

const mock = {
    async registerSample(data) {
        await pause(400);
        const id = crypto.randomUUID();
        mockStore[id] = { id, ...data };
        return { id, ...data };
    },

    async analyze(id) {
        await pause(700); // simule la latence mentionnée dans le sujet
        const sample = mockStore[id];
        if (!sample) throw new Error(`Échantillon introuvable : ${id}`);

        const result = {
            ...sample,
            ...(MOCK_RESULTS[sample.testType] ?? { value: "—", unit: "—", interpretation: "INCONNU" }),
            signature: `BIO-${Date.now()}`,
        };
        mockStore[id] = result;
        return result;
    },
};

const client = config.mockMode ? mock : api;


// ─── Utilitaires ─────────────────────────────────────────────────────────────
const $ = (id) => document.getElementById(id);

function pause(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

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

    // Colore les lignes entre steps
    document.querySelectorAll(".step-line").forEach((line, i) => {
        line.classList.toggle("done", i + 1 < activeStep);
    });
}


// ─── État ─────────────────────────────────────────────────────────────────────
let currentSampleId = null;


// ─── Handlers ─────────────────────────────────────────────────────────────────
$("form-sample").addEventListener("submit", async (e) => {
    e.preventDefault();

    const data = {
        patient:    $("patient").value.trim(),
        testType:   $("testType").value,
        sampleType: $("sampleType").value,
    };

    setStatus("Enregistrement de l'échantillon…");

    try {
        const sample = await client.registerSample(data);
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
    setStatus("Analyse en cours, veuillez patienter…");

    try {
        const result = await client.analyze(currentSampleId);

        $("result-value").textContent  = result.value ?? "—";
        $("result-unit").textContent   = result.unit  ?? "—";
        $("result-patient").textContent = result.patient ?? "—";
        $("result-test").textContent   = result.testType ?? "—";
        $("result-signature").textContent = result.signature ?? "Non validé";

        const interp  = result.interpretation ?? "INCONNU";
        const interpEl = $("result-interpretation");
        const badgeEl  = $("result-interpretation-badge");
        interpEl.textContent        = interp;
        badgeEl.dataset.level       = interp.toLowerCase();

        clearStatus();
        advanceStepper(3);
        showSection("section-result");
    } catch (err) {
        setStatus(err.message, "error");
        $("btn-analyze").disabled = false;
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


// ─── Bandeau mock ─────────────────────────────────────────────────────────────
if (config.mockMode) {
    $("mock-banner").classList.remove("hidden");
}
