const API_BASE = 'http://localhost:8080/api';
let currentUser = null;
let currentAuthMode = 'login';
let draftSubtasks = [];

// Calendar state
let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();
let selectedDateStr = new Date().toISOString().split('T')[0];

// UI Elements
const authSection = document.getElementById('auth-section');
const dashboardSection = document.getElementById('dashboard-section');
const authMsg = document.getElementById('auth-msg');

function switchAuthTab(mode) {
    currentAuthMode = mode;
    document.getElementById('tab-login').classList.toggle('active-tab', mode === 'login');
    document.getElementById('tab-register').classList.toggle('active-tab', mode === 'register');
    document.getElementById('auth-btn').textContent = mode === 'login' ? 'Login' : 'Register';
    authMsg.textContent = '';
}

async function handleAuth(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE}/auth`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mode: currentAuthMode, username, password })
        });
        
        if (response.ok) {
            currentUser = username;
            showDashboard();
        } else {
            const data = await response.json();
            authMsg.textContent = data.message || 'Authentication failed.';
        }
    } catch (err) {
        if (currentAuthMode === 'register') {
            localStorage.setItem('user_' + username, password);
            authMsg.style.color = 'var(--success)';
            authMsg.textContent = 'Registered successfully. Please login.';
            switchAuthTab('login');
        } else {
            if (localStorage.getItem('user_' + username) === password) {
                currentUser = username;
                showDashboard();
            } else {
                authMsg.style.color = 'var(--danger)';
                authMsg.textContent = 'Invalid credentials.';
            }
        }
    }
}

function showDashboard() {
    authSection.classList.add('hidden');
    dashboardSection.classList.remove('hidden');
    
    document.getElementById('sidebar-profile').style.display = 'flex';
    document.getElementById('profile-username').textContent = currentUser;
    document.getElementById('profile-initial').textContent = currentUser.charAt(0).toUpperCase();
    
    showPage('dashboard');
}

function logout() {
    currentUser = null;
    dashboardSection.classList.add('hidden');
    authSection.classList.remove('hidden');
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    document.getElementById('sidebar-profile').style.display = 'none';
}

function showPage(pageId) {
    document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
    document.getElementById(`page-${pageId}`).classList.remove('hidden');
    
    document.querySelectorAll('.sidebar ul li').forEach(li => li.classList.remove('active-nav'));
    const navMap = { 'dashboard': 0, 'tasks': 1, 'calendar': 2, 'add-task': 3, 'add-event': 4, 'reminders': 5 };
    if (navMap[pageId] !== undefined) {
        document.querySelectorAll('.sidebar ul li')[navMap[pageId]].classList.add('active-nav');
    }

    if (pageId === 'tasks') fetchTasks();
    if (pageId === 'dashboard') fetchDashboardStats();
    if (pageId === 'reminders') fetchReminders();
    if (pageId === 'calendar') {
        renderCalendar();
        renderSelectedDateTasks();
        renderWeeklyPreview();
    }
    
    setTimeout(updateCountdowns, 100);
}

function getLocalTasks() {
    return JSON.parse(localStorage.getItem(`tasks_${currentUser}`) || '[]');
}
function saveLocalTasks(tasks) {
    localStorage.setItem(`tasks_${currentUser}`, JSON.stringify(tasks));
}

function renderRecentActivity(tasks) {
    const list = document.getElementById('recent-activity-list');
    list.innerHTML = '';
    
    let activities = [];
    tasks.forEach(t => {
        activities.push({ text: `Created task <b>${t.title}</b>`, time: t.id });
        if (t.completed) {
            activities.push({ text: `Completed task <b>${t.title}</b>`, time: t.id + 1000 });
        }
    });
    
    activities.sort((a, b) => b.time - a.time);
    activities = activities.slice(0, 5); 
    
    if (activities.length === 0) {
        list.innerHTML = '<li>No recent activity.</li>';
        return;
    }

    activities.forEach(a => {
        const d = new Date(a.time);
        list.innerHTML += `<li>${a.text} <span class="time">${d.toLocaleDateString()} at ${d.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span></li>`;
    });
}

function animateValue(id, start, end, duration) {
    if (start === end) {
        document.getElementById(id).innerHTML = end;
        return;
    }
    const obj = document.getElementById(id);
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        obj.innerHTML = Math.floor(progress * (end - start) + start);
        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            obj.innerHTML = end;
        }
    };
    window.requestAnimationFrame(step);
}

async function fetchDashboardStats() {
    let tasks = getLocalTasks();
    let events = getLocalEvents();
    
    const total = tasks.length;
    const completed = tasks.filter(t => t.completed).length;
    const pending = total - completed;
    
    const today = new Date().toISOString().split('T')[0];
    const overdue = tasks.filter(t => !t.completed && t.date < today).length;
    
    animateValue("stat-total-tasks", 0, total, 1000);
    animateValue("stat-completed-tasks", 0, completed, 1000);
    animateValue("stat-pending-tasks", 0, pending, 1000);
    animateValue("stat-overdue-tasks", 0, overdue, 1000);

    const progressPerc = total === 0 ? 0 : Math.round((completed / total) * 100);
    document.getElementById('stat-progress-text').textContent = `${progressPerc}%`;
    document.getElementById('stat-progress-fill').style.width = `${progressPerc}%`;

    // Update Profile Stats
    document.getElementById('profile-total-tasks').textContent = total;
    document.getElementById('profile-completed-tasks').textContent = completed;

    const todaysTasks = tasks.filter(t => t.date === today && !t.completed);
    renderTasks(todaysTasks, 'today-schedule-list');

    const upcomingEvents = events.filter(e => e.date >= today).sort((a,b) => a.date.localeCompare(b.date)).slice(0, 5);
    renderEvents(upcomingEvents, 'upcoming-events-list');

    renderRecentActivity(tasks);
}

// Subtasks Logic
function addSubtaskDraft() {
    const input = document.getElementById('subtask-input');
    const val = input.value.trim();
    if(val) {
        draftSubtasks.push({ id: Date.now(), text: val, completed: false });
        input.value = '';
        renderDraftSubtasks();
    }
}
function renderDraftSubtasks() {
    const list = document.getElementById('draft-subtasks-list');
    list.innerHTML = '';
    draftSubtasks.forEach((st, i) => {
        list.innerHTML += `<li>✔ ${st.text} <button type="button" style="background:transparent; border:none; color:var(--danger); cursor:pointer; margin-left:10px" onclick="removeSubtaskDraft(${i})">✖</button></li>`;
    });
}
function removeSubtaskDraft(index) {
    draftSubtasks.splice(index, 1);
    renderDraftSubtasks();
}

async function handleAddTask(e) {
    e.preventDefault();
    const title = document.getElementById('task-title').value;
    const desc = document.getElementById('task-desc').value;
    const date = document.getElementById('task-date').value;
    const priority = document.getElementById('task-priority').value;
    const category = document.getElementById('task-category').value;
    const color = document.getElementById('task-color').value;
    const recurrence = document.getElementById('task-recurrence').value;

    const task = { 
        id: Date.now(), title, desc, date, priority, category, color, recurrence,
        completed: false, 
        subtasks: [...draftSubtasks]
    };

    const tasks = getLocalTasks();
    tasks.push(task);
    saveLocalTasks(tasks);
    
    draftSubtasks = [];
    renderDraftSubtasks();
    e.target.reset();
    showPage('tasks');
}

// Events Logic
function getLocalEvents() {
    return JSON.parse(localStorage.getItem(`events_${currentUser}`) || '[]');
}
function saveLocalEvents(events) {
    localStorage.setItem(`events_${currentUser}`, JSON.stringify(events));
}

async function handleAddEvent(e) {
    e.preventDefault();
    const title = document.getElementById('event-title').value;
    const desc = document.getElementById('event-desc').value;
    const datetime = document.getElementById('event-datetime').value;
    const priority = document.getElementById('event-priority').value;
    const category = document.getElementById('event-category').value;
    const color = document.getElementById('event-color').value;

    const dateStr = datetime.split('T')[0];

    const event = { 
        id: Date.now(), title, desc, datetime, date: dateStr, priority, category, color
    };

    const events = getLocalEvents();
    events.push(event);
    saveLocalEvents(events);
    
    e.target.reset();
    showPage('dashboard');
}

function renderEvents(events, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = events.length === 0 ? '<p style="color:var(--text-muted)">No upcoming events.</p>' : '';
    
    events.forEach(ev => {
        const div = document.createElement('div');
        div.className = 'task-item';
        let priorityColor = ev.priority === 'HIGH' ? 'var(--danger)' : (ev.priority === 'MEDIUM' ? 'var(--primary)' : 'var(--success)');
        let colorBar = ev.color ? `<div class="task-color-bar" style="background:${ev.color}"></div>` : '';

        div.innerHTML = `
            ${colorBar}
            <div style="padding-left: 10px; width:100%">
                <div style="display:flex; justify-content:space-between; align-items:flex-start">
                    <div>
                        <h4>📅 ${ev.title} <span class="badge" style="color: ${priorityColor}; border: 1px solid currentColor">${ev.priority}</span></h4>
                        <p style="font-size:0.85rem; color:var(--text-muted); margin-top: 4px;">${ev.desc || ''}</p>
                    </div>
                    <button class="btn-delete" title="Delete Event" onclick="deleteEvent(${ev.id})">✖</button>
                </div>
                <div style="margin-top:10px; display:flex; justify-content:space-between; align-items:center">
                    <p style="font-size:0.85rem; color:var(--text-muted)">Time: ${new Date(ev.datetime).toLocaleString()}</p>
                </div>
            </div>
        `;
        container.appendChild(div);
    });
}

function deleteEvent(id) {
    let events = getLocalEvents();
    events = events.filter(x => x.id !== id);
    saveLocalEvents(events);
    refreshCurrentView();
}

async function fetchTasks() {
    const filter = document.getElementById('task-filter').value;
    let tasks = getLocalTasks();

    if (filter === 'completed') tasks = tasks.filter(t => t.completed);
    if (filter === 'pending') tasks = tasks.filter(t => !t.completed);

    renderTasks(tasks, 'all-tasks-list');
}

function renderTasks(tasks, containerId) {
    if (containerId === 'all-tasks-list') {
        renderKanban(tasks);
        updateCountdowns();
        return;
    }
    
    const container = document.getElementById(containerId);
    container.innerHTML = tasks.length === 0 ? '<p style="color:var(--text-muted)">No tasks found.</p>' : '';
    
    tasks.forEach(t => {
        container.appendChild(createTaskElement(t));
    });
    updateCountdowns();
}

function renderKanban(tasks) {
    const board = document.getElementById('kanban-board');
    board.innerHTML = '';
    
    const categories = ['Work', 'Personal', 'Shopping', 'Fitness'];
    tasks.forEach(t => {
        if (!categories.includes(t.category)) categories.push(t.category || 'Other');
    });

    categories.forEach(cat => {
        const catTasks = tasks.filter(t => (t.category || 'Other') === cat);
        
        const col = document.createElement('div');
        col.className = 'kanban-column';
        col.innerHTML = `
            <h3>${cat} <span style="font-size:0.8rem; background:var(--bg-dark); padding:2px 8px; border-radius:10px">${catTasks.length}</span></h3>
            <div class="kanban-tasks" id="cat-${cat}" ondragover="allowDrop(event)" ondragleave="dragLeave(event)" ondrop="drop(event, '${cat}')"></div>
        `;
        
        board.appendChild(col);
        
        const tasksContainer = col.querySelector('.kanban-tasks');
        catTasks.forEach(t => {
            tasksContainer.appendChild(createTaskElement(t));
        });
    });
}

function createTaskElement(t) {
    const div = document.createElement('div');
    const today = new Date().toISOString().split('T')[0];
    const isOverdue = !t.completed && t.date < today;

    div.className = `task-item ${t.completed ? 'completed' : ''} ${isOverdue ? 'overdue' : ''}`;
    div.draggable = true;
    div.id = `task-${t.id}`;
    div.ondragstart = (e) => dragStart(e, t.id);

    let priorityColor = t.priority === 'HIGH' ? 'var(--danger)' : (t.priority === 'MEDIUM' ? 'var(--primary)' : 'var(--success)');
    let colorBar = t.color ? `<div class="task-color-bar" style="background:${t.color}"></div>` : '';

    let subtasksHTML = '';
    let progressHTML = '';
    if (t.subtasks && t.subtasks.length > 0) {
        const completedCount = t.subtasks.filter(st => st.completed).length;
        const total = t.subtasks.length;
        const perc = Math.round((completedCount / total) * 100);
        
        progressHTML = `
            <div class="task-progress">
                <div class="task-progress-bar"><div class="task-progress-fill" style="width: ${perc}%"></div></div>
                <div style="font-size:0.8rem; color:var(--text-muted); text-align:right">${completedCount}/${total} Subtasks</div>
            </div>
        `;
        
        subtasksHTML = '<ul class="subtask-list">';
        t.subtasks.forEach(st => {
            subtasksHTML += `
                <li>
                    <input type="checkbox" ${st.completed ? 'checked' : ''} onchange="toggleSubtask(${t.id}, ${st.id})">
                    <span style="${st.completed ? 'text-decoration:line-through' : ''}">${st.text}</span>
                </li>
            `;
        });
        subtasksHTML += '</ul>';
    }

    let recurrenceBadge = t.recurrence && t.recurrence !== 'none' ? `<span class="badge" style="color:var(--primary)">↻ ${t.recurrence}</span>` : '';

    div.innerHTML = `
        ${colorBar}
        <div style="padding-left: 10px; width:100%">
            <div style="display:flex; justify-content:space-between; align-items:flex-start">
                <div>
                    <h4>${t.title} <span class="badge" style="color: ${priorityColor}; border: 1px solid currentColor">${t.priority}</span> ${recurrenceBadge}</h4>
                    <p style="font-size:0.85rem; color:var(--text-muted); margin-top: 4px;">${t.desc || ''}</p>
                </div>
                <div class="task-actions" style="display:flex">
                    ${!t.completed ? `<button class="btn-complete" title="Mark Complete" onclick="toggleComplete(${t.id})">✔</button>` : ''}
                    <button class="btn-delete" title="Delete Task" onclick="deleteTask(${t.id})">✖</button>
                </div>
            </div>
            
            ${subtasksHTML}
            ${progressHTML}
            
            <div style="margin-top:10px; display:flex; justify-content:space-between; align-items:center">
                <p style="font-size:0.85rem; color:var(--text-muted)">Due: ${t.date} ${isOverdue ? '<strong style="color:var(--danger)">(Overdue)</strong>' : ''}</p>
                ${!t.completed ? `<div class="countdown-timer" data-date="${t.date}T23:59:59">Computing...</div>` : ''}
            </div>
        </div>
    `;
    return div;
}

// Drag and drop logic
let draggedTaskId = null;
function dragStart(e, id) {
    draggedTaskId = id;
    e.dataTransfer.effectAllowed = 'move';
    setTimeout(() => e.target.classList.add('dragging'), 0);
}
function allowDrop(e) {
    e.preventDefault();
    e.currentTarget.classList.add('drag-over');
}
function dragLeave(e) {
    e.currentTarget.classList.remove('drag-over');
}
async function drop(e, newCategory) {
    e.preventDefault();
    e.currentTarget.classList.remove('drag-over');
    if (!draggedTaskId) return;

    let tasks = getLocalTasks();
    let task = tasks.find(t => t.id === draggedTaskId);
    if (task && task.category !== newCategory) {
        task.category = newCategory;
        saveLocalTasks(tasks);
        fetchTasks(); 
        fetchDashboardStats(); 
    }
    draggedTaskId = null;
}

function toggleSubtask(taskId, subtaskId) {
    let tasks = getLocalTasks();
    let task = tasks.find(t => t.id === taskId);
    if (task && task.subtasks) {
        let st = task.subtasks.find(s => s.id === subtaskId);
        if (st) {
            st.completed = !st.completed;
            saveLocalTasks(tasks);
            if (!document.getElementById('page-tasks').classList.contains('hidden')) {
                fetchTasks();
            } else if (!document.getElementById('page-calendar').classList.contains('hidden')) {
                renderSelectedDateTasks();
                renderWeeklyPreview();
            } else {
                fetchDashboardStats();
            }
        }
    }
}

async function toggleComplete(id) {
    let tasks = getLocalTasks();
    const t = tasks.find(x => x.id === id);
    if (t) {
        t.completed = true;
        
        // Handle recurrence
        if (t.recurrence && t.recurrence !== 'none') {
            const nextTask = JSON.parse(JSON.stringify(t)); // deep clone
            nextTask.id = Date.now();
            nextTask.completed = false;
            
            if (nextTask.subtasks) {
                nextTask.subtasks.forEach(st => st.completed = false);
            }
            
            const d = new Date(t.date);
            if (t.recurrence === 'daily') d.setDate(d.getDate() + 1);
            else if (t.recurrence === 'weekly') d.setDate(d.getDate() + 7);
            else if (t.recurrence === 'monthly') d.setMonth(d.getMonth() + 1);
            
            nextTask.date = d.toISOString().split('T')[0];
            tasks.push(nextTask);
        }
    }
    saveLocalTasks(tasks);
    
    refreshCurrentView();
}

async function deleteTask(id) {
    let tasks = getLocalTasks();
    tasks = tasks.filter(x => x.id !== id);
    saveLocalTasks(tasks);
    
    refreshCurrentView();
}

function refreshCurrentView() {
    if (!document.getElementById('page-tasks').classList.contains('hidden')) {
        fetchTasks();
    } else if (!document.getElementById('page-calendar').classList.contains('hidden')) {
        renderCalendar();
        renderSelectedDateTasks();
        renderWeeklyPreview();
    } else {
        fetchDashboardStats();
    }
}

function updateCountdowns() {
    document.querySelectorAll('.countdown-timer').forEach(el => {
        const due = new Date(el.dataset.date).getTime();
        const now = new Date().getTime();
        const diff = due - now;
        
        if (diff < 0) {
            el.innerHTML = `<span style="color:var(--danger)">0d 0h 0m</span>`;
            return;
        }
        
        const d = Math.floor(diff / (1000 * 60 * 60 * 24));
        const h = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        
        el.innerHTML = `⏱ ${d}d ${h}h ${m}m remaining`;
    });
}
setInterval(updateCountdowns, 60000);

// Calendar System
function changeMonth(dir) {
    currentMonth += dir;
    if (currentMonth > 11) { currentMonth = 0; currentYear++; }
    else if (currentMonth < 0) { currentMonth = 11; currentYear--; }
    renderCalendar();
}

function selectDate(dateStr) {
    selectedDateStr = dateStr;
    renderCalendar();
    renderSelectedDateTasks();
}

function renderCalendar() {
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    document.getElementById('calendar-month-year').textContent = `${monthNames[currentMonth]} ${currentYear}`;
    
    const grid = document.getElementById('calendar-grid');
    grid.innerHTML = '';
    
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    
    const todayStr = new Date().toISOString().split('T')[0];
    const tasks = getLocalTasks();
    const events = getLocalEvents();
    
    // Empty cells for alignment
    for(let i=0; i<firstDay; i++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-cell disabled';
        grid.appendChild(cell);
    }
    
    // Days
    for(let day=1; day<=daysInMonth; day++) {
        const d = new Date(currentYear, currentMonth, day);
        // Correct timezone offset issue by formatting manually
        const dateStr = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
        
        const cellTasks = tasks.filter(t => t.date === dateStr);
        const cellEvents = events.filter(e => e.date === dateStr);
        
        const cell = document.createElement('div');
        cell.className = `calendar-cell ${dateStr === todayStr ? 'today' : ''} ${dateStr === selectedDateStr ? 'selected' : ''}`;
        cell.onclick = () => selectDate(dateStr);
        
        let dotsHTML = '<div class="calendar-dots">';
        cellTasks.forEach(t => {
            const color = t.color || 'var(--primary)';
            dotsHTML += `<div class="cal-dot" style="background:${color}" title="${t.title}"></div>`;
        });
        cellEvents.forEach(e => {
            const color = e.color || '#10B981';
            dotsHTML += `<div class="event-dot" style="background:${color}" title="Event: ${e.title}"></div>`;
        });
        dotsHTML += '</div>';
        
        cell.innerHTML = `
            <div class="date-num">${day}</div>
            ${dotsHTML}
        `;
        grid.appendChild(cell);
    }
}

function renderSelectedDateTasks() {
    document.getElementById('selected-date-title').textContent = `Tasks & Events for ${selectedDateStr}`;
    const tasks = getLocalTasks().filter(t => t.date === selectedDateStr);
    const events = getLocalEvents().filter(e => e.date === selectedDateStr);
    
    const container = document.getElementById('selected-date-tasks');
    container.innerHTML = '';
    renderTasks(tasks, 'selected-date-tasks');
    if (events.length > 0) {
        const title = document.createElement('h4');
        title.style.marginTop = '15px';
        title.style.marginBottom = '10px';
        title.textContent = 'Events';
        container.appendChild(title);
        events.forEach(ev => {
            const tempDiv = document.createElement('div');
            tempDiv.id = `temp-event-${ev.id}`;
            container.appendChild(tempDiv);
            renderEvents([ev], tempDiv.id);
        });
    }
}

function renderWeeklyPreview() {
    const list = document.getElementById('weekly-preview-list');
    const tasks = getLocalTasks();
    const today = new Date();
    
    let upcomingTasks = [];
    
    for(let i=0; i<7; i++) {
        const d = new Date(today);
        d.setDate(today.getDate() + i);
        const dateStr = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
        const dayTasks = tasks.filter(t => t.date === dateStr && !t.completed);
        upcomingTasks.push(...dayTasks);
    }
    
    renderTasks(upcomingTasks, 'weekly-preview-list');
}

// Reminders System
function getLocalReminders() {
    return JSON.parse(localStorage.getItem(`rem_${currentUser}`) || '[]');
}
function handleAddReminder(e) {
    e.preventDefault();
    const msg = document.getElementById('rem-msg').value;
    const time = document.getElementById('rem-time').value;
    const priority = document.getElementById('rem-priority').value;
    const rems = getLocalReminders();
    rems.push({ id: Date.now(), msg, time, priority, notified: false });
    localStorage.setItem(`rem_${currentUser}`, JSON.stringify(rems));
    e.target.reset();
    fetchReminders();
}

function fetchReminders() {
    const rems = getLocalReminders();
    const upcomingContainer = document.getElementById('reminders-upcoming-list');
    const overdueContainer = document.getElementById('reminders-overdue-list');
    if(!upcomingContainer || !overdueContainer) return;
    
    upcomingContainer.innerHTML = '';
    overdueContainer.innerHTML = '';
    
    let upcomingCount = 0;
    let overdueCount = 0;
    
    const now = new Date().getTime();

    rems.sort((a,b) => new Date(a.time) - new Date(b.time)).forEach(r => {
        const remTime = new Date(r.time).getTime();
        const isOverdue = remTime < now;
        
        const div = document.createElement('div');
        div.className = `task-item ${isOverdue ? 'overdue' : ''}`;
        
        let priorityColor = r.priority === 'HIGH' ? 'var(--danger)' : (r.priority === 'MEDIUM' ? 'var(--primary)' : 'var(--success)');
        
        div.innerHTML = `
            <div style="width:100%">
                <div style="display:flex; justify-content:space-between;">
                    <h4>${r.msg} <span class="badge" style="color: ${priorityColor}; border: 1px solid currentColor">${r.priority || 'MEDIUM'}</span></h4>
                    <button class="btn-delete" title="Delete Reminder" onclick="deleteReminder(${r.id})">✖</button>
                </div>
                <div style="margin-top:10px; display:flex; justify-content:space-between; align-items:center">
                    <p style="font-size:0.9rem; color:var(--text-muted); margin-top: 4px;">At ${new Date(r.time).toLocaleString()}</p>
                    ${!isOverdue ? `<div class="countdown-timer" data-date="${r.time}">Computing...</div>` : ''}
                </div>
            </div>`;
            
        if (isOverdue) {
            overdueContainer.appendChild(div);
            overdueCount++;
        } else {
            upcomingContainer.appendChild(div);
            upcomingCount++;
        }
    });
    
    if (upcomingCount === 0) upcomingContainer.innerHTML = '<p style="color:var(--text-muted)">No upcoming reminders.</p>';
    if (overdueCount === 0) overdueContainer.innerHTML = '<p style="color:var(--text-muted)">No overdue reminders.</p>';
    
    updateCountdowns();
}

function deleteReminder(id) {
    let rems = getLocalReminders();
    rems = rems.filter(r => r.id !== id);
    localStorage.setItem(`rem_${currentUser}`, JSON.stringify(rems));
    fetchReminders();
}

// Toasts
function showToast(title, message) {
    const container = document.getElementById('toast-container');
    if(!container) return;
    
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <div class="toast-content">
            <h4>${title}</h4>
            <p>${message}</p>
        </div>
        <button class="toast-close" onclick="this.parentElement.classList.add('fade-out'); setTimeout(() => this.parentElement.remove(), 300)">✖</button>
    `;
    container.appendChild(toast);
    
    setTimeout(() => {
        if(toast.parentElement) {
            toast.classList.add('fade-out');
            setTimeout(() => toast.remove(), 300);
        }
    }, 5000);
}

// Check reminders periodically
setInterval(() => {
    if(!currentUser) return;
    let rems = getLocalReminders();
    let updated = false;
    const now = new Date().getTime();
    
    rems.forEach(r => {
        if (!r.notified && new Date(r.time).getTime() <= now) {
            showToast('Reminder Alert!', r.msg);
            r.notified = true;
            updated = true;
        }
    });
    
    if (updated) {
        localStorage.setItem(`rem_${currentUser}`, JSON.stringify(rems));
        if (!document.getElementById('page-reminders').classList.contains('hidden')) {
            fetchReminders();
        } else {
            // Also refresh stats/dashboard if needed
            fetchDashboardStats();
        }
    }
}, 5000); // Check every 5s
