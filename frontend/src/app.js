"use strict";

const config = window.LABOTRACK_CONFIG ?? { mockMode: true };


// Appels vers nginx, qui proxy vers service1 et service3.
// POST /api/register bloque jusqu'à ce que toute la chaîne soit terminée
// (service1 → service2 → service3), donc quand ça répond l'ID est utilisable.
const api = {
    async registerSample(data) {
        const res = await fetch("/api/register", {
            method:  "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(data),
        });
        console.log(res);
        if (!res.ok) throw new Error(`Enregistrement échoué (HTTP ${res.status})`);
        return res.json();
    },

    async getResult(id) {
        const res = await fetch(`/api/results/${id}`);
        if (!res.ok) throw new Error(`Résultat introuvable (HTTP ${res.status})`);
        return res.json();
    },
};


const MOCK_RESULTS = {
    GLYCEMIE:        { value: "0.86", unit: "g/L",  interpretation: "Normal" },
    NFS:             { value: "4.5",  unit: "T/L",  interpretation: "Normal" },
    BILAN_HEPATIQUE: { value: "42",   unit: "UI/L", interpretation: "High"   },
    BILAN_RENAL:     { value: "7.2",  unit: "mg/L", interpretation: "Low"    },
};

const mockStore = {};

const mock = {
    async registerSample(data) {
        await pause(1200);
        const id = crypto.randomUUID();
        const resultTemplate = MOCK_RESULTS[data.testType]
            ?? { value: "—", unit: "—", interpretation: "Inconnu" };
        mockStore[id] = { id, ...data, ...resultTemplate, signature: randomToken(10) };
        return { id, patient: data.patient, testType: data.testType, sampleType: data.sampleType };
    },

    async getResult(id) {
        await pause(200);
        const result = mockStore[id];
        if (!result) throw new Error(`Résultat introuvable pour l'ID ${id}`);
        return result;
    },
};

const client = config.mockMode ? mock : api;


const $ = (id) => document.getElementById(id);

function pause(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function randomToken(length) {
    return Array.from(crypto.getRandomValues(new Uint8Array(length)))
        .map((b) => b.toString(36))
        .join("")
        .slice(0, length)
        .toUpperCase();
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
        const sample = await client.registerSample(data);
        currentSampleId = sample.id;

        console.log(sample);
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
        const result = await client.getResult(currentSampleId);

        $("result-value").textContent     = result.value          ?? "—";
        $("result-unit").textContent      = result.unit           ?? "—";
        $("result-patient").textContent   = result.patient        ?? "—";
        $("result-test").textContent      = result.testType       ?? "—";
        $("result-signature").textContent = result.signature      ?? "Non validé";

        const interp  = result.interpretation ?? "INCONNU";
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

$("btn-reset").addEventListener("click", () => {
    currentSampleId = null;
    $("form-sample").reset();
    $("btn-analyze").disabled = false;
    clearStatus();
    advanceStepper(1);
    showSection("section-registration");
});


if (config.mockMode) {
    $("mock-banner").classList.remove("hidden");
}
