(function () {
  "use strict";

  function el(id) {
    return document.getElementById(id);
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function showSkeleton(container) {
    container.classList.remove("hidden");
    container.innerHTML =
      '<div class="loading-state"><div class="spinner"></div>' +
      "<span>Querying CognoDB&hellip;</span></div>";
  }

  function renderError(container, message) {
    container.classList.remove("hidden");
    container.innerHTML =
      '<div class="error-box">' + escapeHtml(message) + "</div>";
  }

  function renderEmpty(container, message) {
    container.classList.remove("hidden");
    container.innerHTML =
      '<div class="empty-state compact"><p>' + escapeHtml(message) + "</p></div>";
  }

  async function fetchJson(url) {
    const res = await fetch(url);
    if (!res.ok) {
      let detail = "Request failed (" + res.status + ")";
      try {
        const body = await res.json();
        if (body && body.message) detail = body.message;
      } catch (ignored) {
        /* keep default message */
      }
      throw new Error(detail);
    }
    return res.json();
  }

  function guardSame(selectA, selectB, button) {
    function update() {
      button.disabled = !!selectA.value && selectA.value === selectB.value;
    }
    selectA.addEventListener("change", update);
    selectB.addEventListener("change", update);
    update();
  }

  // ---- Skill learning path ----------------------------------------------------
  function initSkillPath() {
    const from = el("pathFrom");
    const to = el("pathTo");
    const searchBtn = el("pathSearch");
    const results = el("pathResults");
    if (!from || !to || !searchBtn || !results) return;

    guardSame(from, to, searchBtn);

    searchBtn.addEventListener("click", async function () {
      if (!from.value || !to.value) return;
      showSkeleton(results);
      try {
        const data = await fetchJson(
          "/api/pathfinder/skill-path?from=" + encodeURIComponent(from.value) +
            "&to=" + encodeURIComponent(to.value)
        );
        if (!data.steps || data.steps.length === 0) {
          renderEmpty(results, data.message || "No learning path found between these skills.");
        } else {
          renderSkillPath(results, data);
        }
      } catch (err) {
        renderError(results, err.message);
      }
    });
  }

  function renderSkillPath(container, data) {
    const items = data.steps
      .map(function (step) {
        const months = step.monthsToLearn > 0 ? "+" + step.monthsToLearn + " mo" : "start";
        return (
          '<li class="path-step">' +
          '<span class="step-number">' + step.step + "</span>" +
          '<div class="step-info">' +
          '<div class="step-name">' + escapeHtml(step.skillName) + "</div>" +
          '<div class="step-category">' + escapeHtml(step.category) + "</div>" +
          "</div>" +
          '<span class="step-months">' + months + "</span>" +
          "</li>"
        );
      })
      .join("");

    container.innerHTML =
      '<ul class="path-steps">' + items + "</ul>" +
      '<div class="path-summary">' +
      '<div class="spinner" style="width:15px;height:15px;border-width:2px"></div>' +
      "<span>Estimated learning time: " + data.totalMonths + " months</span>" +
      "</div>";
  }

  // ---- Bridge people ------------------------------------------------------------
  function initBridgePeople() {
    const roleA = el("bridgeRoleA");
    const roleB = el("bridgeRoleB");
    const searchBtn = el("bridgeSearch");
    const results = el("bridgeResults");
    if (!roleA || !roleB || !searchBtn || !results) return;

    guardSame(roleA, roleB, searchBtn);

    searchBtn.addEventListener("click", async function () {
      if (!roleA.value || !roleB.value) return;
      showSkeleton(results);
      try {
        const data = await fetchJson(
          "/api/pathfinder/bridge-people?roleA=" + encodeURIComponent(roleA.value) +
            "&roleB=" + encodeURIComponent(roleB.value)
        );
        if (!data || data.length === 0) {
          renderEmpty(results, "No one bridges these two roles through shared skills yet.");
        } else {
          renderBridgePeople(results, data);
        }
      } catch (err) {
        renderError(results, err.message);
      }
    });
  }

  function renderBridgePeople(container, data) {
    const items = data
      .map(function (person) {
        return (
          '<li class="bridge-item">' +
          '<div class="bridge-avatar">' + escapeHtml((person.personName || "?").charAt(0)) + "</div>" +
          '<div class="bridge-info">' +
          '<div class="bridge-name">' + escapeHtml(person.personName) + "</div>" +
          '<div class="bridge-detail">Bridges ' +
          '<span class="role-badge">' + escapeHtml(person.roleATitle) + "</span> and " +
          '<span class="role-badge">' + escapeHtml(person.roleBTitle) + "</span> via ' +
          "<strong>" + escapeHtml(person.connectingSkill) + "</strong></div>" +
          "</div>" +
          "</li>"
        );
      })
      .join("");
    container.innerHTML = '<ul class="bridge-list">' + items + "</ul>";
  }

  // ---- Skill gap -----------------------------------------------------------------
  function initSkillGap() {
    const person = el("gapPerson");
    const role = el("gapRole");
    const analyzeBtn = el("gapAnalyzeBtn");
    const results = el("gapResults");
    if (!person || !role || !analyzeBtn || !results) return;

    analyzeBtn.addEventListener("click", async function () {
      if (!person.value || !role.value) return;
      showSkeleton(results);
      try {
        const data = await fetchJson(
          "/api/pathfinder/skill-gap?personId=" + encodeURIComponent(person.value) +
            "&roleId=" + encodeURIComponent(role.value)
        );
        if (!data || data.length === 0) {
          renderEmpty(results, "No skill gaps found — this person meets the role requirements.");
        } else {
          renderSkillGap(results, data, person);
        }
      } catch (err) {
        renderError(results, err.message);
      }
    });
  }

  function renderSkillGap(container, data, personSelect) {
    const personName =
      personSelect && personSelect.selectedOptions.length
        ? personSelect.selectedOptions[0].text
        : "This person";

    const items = data
      .map(function (gap) {
        return (
          '<li class="gap-item">' +
          '<div class="gap-icon">&#9888;</div>' +
          '<div class="gap-info" style="flex:1;min-width:0">' +
          '<div class="gap-name">' + escapeHtml(gap.skillName) + "</div>" +
          '<div class="gap-category">' + escapeHtml(gap.category) + "</div>" +
          "</div>" +
          '<span class="gap-required">Needs: ' + escapeHtml(gap.requiredProficiency) + "</span>" +
          "</li>"
        );
      })
      .join("");

    container.innerHTML =
      '<div class="section-desc" style="margin-bottom:8px">Skill gaps for <strong>' +
      escapeHtml(personName) + "</strong>:</div>" +
      '<ul class="gap-list">' + items + "</ul>";
  }

  // ---- boot ------------------------------------------------------------------------
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  function init() {
    initSkillPath();
    initBridgePeople();
    initSkillGap();
  }
})();