const SLOT_DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
const SLOT_TIMES = ["09:00", "12:00", "15:00", "18:00"];
const portalState = { tags: [], students: [], mentors: [], pairs: [], sessions: [] };
let selectedRole = "student";
let activeStudentId = null;

const byId = (id) => document.getElementById(id);
const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
}[char]));

function notify(message, error = false) {
    const toast = byId("toast");
    toast.textContent = message;
    toast.classList.toggle("error", error);
    toast.classList.add("show");
    window.clearTimeout(notify.timer);
    notify.timer = window.setTimeout(() => toast.classList.remove("show"), 3200);
}

function selectRole(role) {
    selectedRole = role;
    const isMentor = role === "mentor";
    byId("roleSlider").classList.toggle("mentor", isMentor);
    byId("studentRoleBtn").classList.toggle("active", !isMentor);
    byId("mentorRoleBtn").classList.toggle("active", isMentor);
    byId("roleDescription").classList.toggle("mentor", isMentor);
    byId("roleDescription").innerHTML = isMentor
        ? "👨‍🏫<div><b>Alumni Mentor</b><small>Share your expertise, interests, and available weekly slots.</small></div>"
        : "🎓<div><b>Student</b><small>Choose interests and times when you can meet a mentor.</small></div>";
    byId("registrationFields").innerHTML = isMentor
        ? '<label class="registration-extra">Maximum mentees<input name="maxMentees" type="number" min="1" value="3" required></label>'
        : '<label class="registration-extra">Department<input name="department" required></label>';
    byId("slotLegend").innerHTML = `${isMentor ? "Mentor availability" : "Preferred meeting times"} <small>Select at least one one-hour slot</small>`;
    byId("loginButtonText").textContent = isMentor ? "Register as mentor" : "Register as student";
}

function renderChoices() {
    byId("interestOptions").innerHTML = portalState.tags.length
        ? portalState.tags.map((tag) => `<label class="choice-pill"><input type="checkbox" name="interestTagId" value="${tag.id}"><span>${escapeHtml(tag.name)}</span></label>`).join("")
        : '<p class="choice-help">No saved tags yet. Add interests below; matching tags are created automatically.</p>';

    byId("slotOptions").innerHTML = SLOT_DAYS.map((day) => `<div class="slot-day"><b>${day.slice(0, 3)}</b>${SLOT_TIMES.map((time) => {
        const slot = `${day}_${time}`;
        return `<label title="${day} at ${time}"><input type="checkbox" name="availabilitySlot" value="${slot}"><span>${time}</span></label>`;
    }).join("")}</div>`).join("");
}

function showPage(page) {
    document.querySelectorAll(".workspace-panel").forEach((panel) => panel.classList.toggle("active", panel.dataset.view === page));
    document.querySelectorAll(".workspace-tab").forEach((tab) => tab.classList.toggle("active", tab.dataset.panel === page));
}

function profileCard(profile, mentor = false) {
    const interests = (profile.interestTags || []).map((tag) => tag.name).join(", ") || "No interests listed";
    const slots = mentor ? profile.availableSlots : profile.preferredSlots;
    const prettySlots = (slots || []).map((slot) => slot.replace("_", " ")).join(" · ") || "No slots listed";
    return `<article class="workspace-record"><div><h4>${escapeHtml(profile.name)}</h4><p>${escapeHtml(profile.email)}</p><p><b>Interests:</b> ${escapeHtml(interests)}</p><p><b>${mentor ? "Available" : "Preferred"} slots:</b> ${escapeHtml(prettySlots)}</p>${mentor ? `<p><b>Capacity:</b> ${profile.currentMentees ?? 0}/${profile.maxMentees} active mentees</p>` : `<p><b>Department:</b> ${escapeHtml(profile.department)}</p>`}</div></article>`;
}

function renderDirectory() {
    byId("studentsList").innerHTML = portalState.students.map((profile) => profileCard(profile)).join("") || '<p class="workspace-empty">No students registered yet.</p>';
    byId("mentorsList").innerHTML = portalState.mentors.map((profile) => profileCard(profile, true)).join("") || '<p class="workspace-empty">No alumni mentors registered yet.</p>';
    const current = byId("matchStudent").value || activeStudentId || "";
    byId("matchStudent").innerHTML = '<option value="">Choose a student</option>' + portalState.students.map((student) => `<option value="${student.id}">${escapeHtml(student.name)} (#${student.id})</option>`).join("");
    if (portalState.students.some((student) => String(student.id) === String(current))) byId("matchStudent").value = current;
    const currentPair = byId("sessionPair").value;
    byId("sessionPair").innerHTML = '<option value="">Choose a mentorship</option>' + portalState.pairs.filter((pair) => pair.status === "ACTIVE").map((pair) => `<option value="${pair.id}">${escapeHtml(pair.student.name)} + ${escapeHtml(pair.alumni.name)} (#${pair.id})</option>`).join("");
    if (portalState.pairs.some((pair) => String(pair.id) === currentPair && pair.status === "ACTIVE")) byId("sessionPair").value = currentPair;
    populateSessionSlots();
}

function localDateTimeValue(date) {
    const part = (value) => String(value).padStart(2, "0");
    return `${date.getFullYear()}-${part(date.getMonth() + 1)}-${part(date.getDate())}T${part(date.getHours())}:${part(date.getMinutes())}`;
}

function populateSessionSlots() {
    const pair = portalState.pairs.find((item) => String(item.id) === byId("sessionPair").value);
    const select = byId("sessionDate");
    if (!pair) {
        select.innerHTML = '<option value="">Choose a mentorship first</option>';
        return;
    }

    const sharedSlots = (pair.student.preferredSlots || []).filter((slot) => (pair.alumni.availableSlots || []).includes(slot));
    const choices = [];
    const now = new Date();
    for (const slot of sharedSlots) {
        const separator = slot.lastIndexOf("_");
        const day = slot.slice(0, separator);
        const [hour, minute] = slot.slice(separator + 1).split(":").map(Number);
        const dayIndex = SLOT_DAYS.indexOf(day);
        if (dayIndex < 0) continue;
        const offset = (dayIndex - (now.getDay() + 6) % 7 + 7) % 7;
        const candidate = new Date(now.getFullYear(), now.getMonth(), now.getDate() + offset, hour, minute, 0, 0);
        if (candidate <= now) candidate.setDate(candidate.getDate() + 7);

        for (let week = 0; week < 6; week += 1) {
            const value = localDateTimeValue(candidate);
            const alreadyBooked = portalState.sessions.some((session) => {
                if ((session.status || "SCHEDULED") !== "SCHEDULED" || String(session.sessionDate).slice(0, 16) !== value) return false;
                const scheduledPair = session.mentorshipPair || {};
                return scheduledPair.student?.id === pair.student.id || scheduledPair.alumni?.id === pair.alumni.id;
            });
            if (!alreadyBooked) choices.push({ value, date: new Date(candidate) });
            candidate.setDate(candidate.getDate() + 7);
        }
    }

    choices.sort((first, second) => first.date - second.date);
    select.innerHTML = '<option value="">Choose an available date and time</option>' + choices.map(({ value, date }) => {
        const label = date.toLocaleString(undefined, { weekday: "short", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit", hourCycle: "h23" });
        return `<option value="${value}">${escapeHtml(label)}</option>`;
    }).join("");
    if (!choices.length) select.innerHTML = '<option value="">No shared slots available in the next six weeks</option>';
}

function renderSessions() {
    byId("sessionsList").innerHTML = portalState.sessions.map((session) => {
        const pair = session.mentorshipPair || {};
        const personNames = [pair.student?.name, pair.alumni?.name].filter(Boolean).map(escapeHtml).join(" + ");
        const status = session.status || "SCHEDULED";
        return `<article class="workspace-record session-record"><div><span class="status-pill ${status.toLowerCase()}">${escapeHtml(status)}</span><h4>${escapeHtml(session.topic)}</h4><p>${escapeHtml(personNames)} · ${escapeHtml(String(session.sessionDate).replace("T", " "))} · ${session.durationMinutes} min</p></div>${status === "SCHEDULED" ? `<button class="small-action" data-session-status="COMPLETED" data-session-id="${session.id}">Mark completed</button><button class="small-action muted-action" data-session-status="CANCELLED" data-session-id="${session.id}">Cancel</button>` : ""}</article>`;
    }).join("") || '<p class="workspace-empty">No sessions scheduled yet.</p>';
}

async function refreshData() {
    try {
        const [tags, students, mentors, pairs, sessions] = await Promise.all([
            tagAPI.getAll(), studentAPI.getAll(), alumniAPI.getAll(), mentorshipAPI.getAll(), sessionAPI.getAll()
        ]);
        portalState.tags = tags || [];
        portalState.students = students || [];
        portalState.mentors = mentors || [];
        portalState.pairs = pairs || [];
        portalState.sessions = sessions || [];
        byId("apiStatus").textContent = "Backend connected";
        byId("apiDot").classList.add("online");
        renderChoices();
        renderDirectory();
        renderSessions();
        return true;
    } catch (error) {
        byId("apiStatus").textContent = "Backend unavailable";
        byId("apiDot").classList.remove("online");
        notify(error.message, true);
        return false;
    }
}

function parseTags() {
    const selected = [...document.querySelectorAll('input[name="interestTagId"]:checked')]
        .map((input) => ({ id: Number(input.value) }));
    const entered = byId("customTags").value.split(",").map((tag) => tag.trim()).filter(Boolean);
    const enteredTags = [...new Map(entered.map((name) => [name.toLowerCase(), name])).values()]
        .map((name) => {
            const existing = portalState.tags.find((tag) => tag.name.toLowerCase() === name.toLowerCase());
            return existing ? { id: existing.id } : { name };
        });
    return [...new Map([...selected, ...enteredTags].map((tag) => [tag.id ? `id:${tag.id}` : `name:${tag.name.toLowerCase()}`, tag])).values()];
}

byId("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const profile = {
        name: byId("profileName").value.trim(),
        email: byId("profileEmail").value.trim(),
        interestTags: parseTags()
    };
    const slots = [...document.querySelectorAll('input[name="availabilitySlot"]:checked')].map((input) => input.value);
    if (!profile.interestTags.length) return notify("Choose or add at least one interest.", true);
    if (!slots.length) return notify("Choose at least one weekly availability slot.", true);

    if (selectedRole === "student") {
        profile.department = form.querySelector('[name="department"]').value.trim();
        profile.preferredSlots = slots;
    } else {
        profile.maxMentees = Number(form.querySelector('[name="maxMentees"]').value);
        profile.availableSlots = slots;
    }

    const submit = form.querySelector('[type="submit"]');
    submit.disabled = true;
    try {
        const profiles = selectedRole === "student" ? portalState.students : portalState.mentors;
        const existing = profiles.find((item) => item.email.toLowerCase() === profile.email.toLowerCase());
        const api = selectedRole === "student" ? studentAPI : alumniAPI;
        const saved = existing ? await api.update(existing.id, profile) : await api.create(profile);
        if (selectedRole === "student") activeStudentId = saved.id;
        byId("loginScreen").style.display = "none";
        byId("application").classList.remove("hidden");
        byId("welcomeUser").innerHTML = `Welcome, ${escapeHtml(saved.name)} <small>${selectedRole === "mentor" ? "Alumni mentor profile" : "Student profile"}</small>`;
        await refreshData();
        showPage("matches");
        notify(existing ? "Profile updated successfully." : "Profile registered successfully.");
    } catch (error) {
        notify(error.message, true);
    } finally {
        submit.disabled = false;
    }
});

byId("findMatchesBtn").addEventListener("click", async () => {
    const studentId = byId("matchStudent").value;
    if (!studentId) return notify("Choose a student first.", true);
    const output = byId("matchesList");
    output.innerHTML = '<p class="workspace-empty">Finding mentors with overlapping interests and free matching slots…</p>';
    try {
        const matches = await mentorshipAPI.suggestions(studentId);
        output.innerHTML = matches.map((match) => `<article class="workspace-record match-record"><div><span class="match-score">${match.matchingScore} shared interest${match.matchingScore === 1 ? "" : "s"}</span><h4>${escapeHtml(match.name)}</h4><p><b>Interests:</b> ${escapeHtml((match.matchingInterests || []).join(", "))}</p><p><b>Shared slots:</b> ${escapeHtml((match.matchingSlots || []).map((slot) => slot.replace("_", " ")).join(" · "))}</p><p><b>Capacity:</b> ${match.currentMentees}/${match.maxMentees} active mentees</p></div><button class="small-action request-mentor" data-student-id="${studentId}" data-alumni-id="${match.alumniId}">Request mentorship</button></article>`).join("") || '<p class="workspace-empty">No mentors match both your interests and preferred slots, or all matches are at capacity.</p>';
    } catch (error) {
        output.innerHTML = `<p class="workspace-empty">${escapeHtml(error.message)}</p>`;
    }
});

byId("matchesList").addEventListener("click", async (event) => {
    const button = event.target.closest(".request-mentor");
    if (!button) return;
    button.disabled = true;
    try {
        await mentorshipAPI.create({ studentId: button.dataset.studentId, alumniId: button.dataset.alumniId });
        notify("Mentorship request accepted.");
        await refreshData();
        byId("findMatchesBtn").click();
    } catch (error) {
        notify(error.message, true);
        button.disabled = false;
    }
});

byId("sessionForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = new FormData(form);
    const sessionDate = values.get("sessionDate");
    if (!sessionDate) return notify("Choose one of the listed shared slots.", true);
    const date = new Date(sessionDate);
    const slot = `${SLOT_DAYS[(date.getDay() + 6) % 7]}_${String(date.getHours()).padStart(2, "0")}:00`;
    const pair = portalState.pairs.find((item) => String(item.id) === String(values.get("mentorshipPairId")));
    if (!pair) return notify("Choose an active mentorship.", true);
    if (!pair.student.preferredSlots.includes(slot) || !pair.alumni.availableSlots.includes(slot)) {
        return notify(`That start time is not shared. Choose one of the matched slots for ${SLOT_DAYS[(date.getDay() + 6) % 7]}.`, true);
    }
    try {
        await sessionAPI.create({
            mentorshipPairId: values.get("mentorshipPairId"),
            topic: values.get("topic"),
            sessionDate,
            durationMinutes: Number(values.get("durationMinutes")),
            status: "SCHEDULED"
        });
        form.reset();
        form.querySelector('[name="durationMinutes"]').value = "60";
        await refreshData();
        notify("Session scheduled.");
    } catch (error) {
        notify(error.message, true);
    }
});

byId("sessionPair").addEventListener("change", populateSessionSlots);

byId("sessionsList").addEventListener("click", async (event) => {
    const button = event.target.closest("[data-session-status]");
    if (!button) return;
    const session = portalState.sessions.find((item) => String(item.id) === button.dataset.sessionId);
    if (!session) return;
    try {
        await sessionAPI.update(session.id, {
            topic: session.topic,
            sessionDate: session.sessionDate,
            durationMinutes: session.durationMinutes,
            status: button.dataset.sessionStatus
        });
        await refreshData();
        notify(`Session marked ${button.dataset.sessionStatus.toLowerCase()}.`);
        loadReports();
    } catch (error) {
        notify(error.message, true);
    }
});

async function loadReports() {
    try {
        const [months, mentorships] = await Promise.all([mentorshipAPI.monthlyReport(), mentorshipAPI.report()]);
        byId("monthlyReport").innerHTML = months.length
            ? `<div class="report-row report-head"><b>Month</b><b>Held</b><b>Scheduled</b><b>Completed</b><b>Cancelled</b></div>${months.map((month) => `<div class="report-row"><b>${escapeHtml(month.month)}</b><span>${month.sessionsHeld}</span><span>${month.scheduledSessions}</span><span>${month.completedSessions}</span><span>${month.cancelledSessions}</span></div>`).join("")}`
            : '<p class="workspace-empty">No sessions to report yet.</p>';
        byId("mentorshipReport").innerHTML = mentorships.map((item) => `<article class="workspace-record"><div><h4>${escapeHtml(item.student)} + ${escapeHtml(item.alumni)}</h4><p>Status: ${escapeHtml(item.status)} · ${item.totalSessions} total sessions</p></div></article>`).join("") || '<p class="workspace-empty">No mentorships yet.</p>';
    } catch (error) {
        byId("monthlyReport").innerHTML = `<p class="workspace-empty">${escapeHtml(error.message)}</p>`;
    }
}

document.querySelectorAll(".workspace-tab").forEach((tab) => tab.addEventListener("click", () => {
    showPage(tab.dataset.panel);
    if (tab.dataset.panel === "reports") loadReports();
}));
byId("refreshReportBtn").addEventListener("click", loadReports);

function logout() {
    byId("application").classList.add("hidden");
    byId("loginScreen").style.display = "flex";
    byId("loginForm").reset();
    byId("profileName").value = "";
    byId("profileEmail").value = "";
    byId("customTags").value = "";
    selectRole("student");
    renderChoices();
}

selectRole("student");
renderChoices();
refreshData();