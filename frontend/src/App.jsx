import { useState, useEffect, useCallback } from 'react';

const API_BASE = 'http://localhost:8081/api/tasks';

const STATUS_OPTIONS = ['TODO', 'IN_PROGRESS', 'DONE'];

const STATUS_LABELS = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
};

// ─── API helper ───────────────────────────────────────────────────────────────

async function apiRequest(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  // DELETE returns 204 with no body
  if (res.status === 204) return null;

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.message || `Request failed with status ${res.status}`);
  }

  return data;
}

// ─── Initial form state ──────────────────────────────────────────────────────

const emptyForm = { title: '', description: '', status: 'TODO' };

// ─── App Component ───────────────────────────────────────────────────────────

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({ ...emptyForm });
  const [fieldErrors, setFieldErrors] = useState({});
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // ── Fetch tasks ────────────────────────────────────────────────────────────

  const fetchTasks = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await apiRequest(API_BASE);
      setTasks(data);
    } catch (err) {
      setError('Failed to load tasks: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  // Auto-dismiss success message
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  // ── Validate form ─────────────────────────────────────────────────────────

  function validate() {
    const errors = {};
    const trimmedTitle = form.title.trim();

    if (!trimmedTitle) {
      errors.title = 'Title is required';
    } else if (trimmedTitle.length < 3) {
      errors.title = 'Title must be at least 3 characters';
    } else if (trimmedTitle.length > 100) {
      errors.title = 'Title must not exceed 100 characters';
    }

    if (form.description && form.description.length > 500) {
      errors.description = 'Description must not exceed 500 characters';
    }

    if (!STATUS_OPTIONS.includes(form.status)) {
      errors.status = 'Invalid status';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  // ── Handle form submission ────────────────────────────────────────────────

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    setError('');
    setSuccess('');

    const body = JSON.stringify({
      title: form.title.trim(),
      description: form.description.trim(),
      status: form.status,
    });

    try {
      if (editingId) {
        await apiRequest(`${API_BASE}/${editingId}`, { method: 'PUT', body });
        setSuccess('Task updated successfully');
      } else {
        await apiRequest(API_BASE, { method: 'POST', body });
        setSuccess('Task created successfully');
      }
      resetForm();
      await fetchTasks();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  // ── Edit task ─────────────────────────────────────────────────────────────

  function handleEdit(task) {
    setEditingId(task.id);
    setForm({
      title: task.title,
      description: task.description || '',
      status: task.status,
    });
    setFieldErrors({});
    setError('');
    setSuccess('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // ── Delete task ───────────────────────────────────────────────────────────

  async function handleDelete(task) {
    if (!window.confirm(`Are you sure you want to delete "${task.title}"?`)) {
      return;
    }

    setError('');
    setSuccess('');
    try {
      await apiRequest(`${API_BASE}/${task.id}`, { method: 'DELETE' });
      setSuccess('Task deleted successfully');
      // If we were editing the deleted task, reset the form
      if (editingId === task.id) resetForm();
      await fetchTasks();
    } catch (err) {
      setError('Failed to delete task: ' + err.message);
    }
  }

  // ── Reset form ────────────────────────────────────────────────────────────

  function resetForm() {
    setForm({ ...emptyForm });
    setEditingId(null);
    setFieldErrors({});
  }

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div className="app">
      <header className="app-header">
        <img src="/logo.png" alt="Safaricom Ethiopia" className="logo" />
        <div className="app-header-text">
          <h1>Task Manager</h1>
          <p>Safaricom Ethiopia — Technical Assessment</p>
        </div>
        <img src="/logo.png" alt="Safaricom Ethiopia" className="logo" />
      </header>

      {/* Messages */}
      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      {/* Form */}
      <form className="task-form" onSubmit={handleSubmit} noValidate>
        <h2>{editingId ? '✏️ Edit Task' : '➕ Create New Task'}</h2>

        <div className="form-group">
          <label htmlFor="title">Title *</label>
          <input
            id="title"
            type="text"
            placeholder="Enter task title (3–100 characters)"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            maxLength={101}
          />
          {fieldErrors.title && (
            <div className="field-error">{fieldErrors.title}</div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            placeholder="Enter task description (optional, max 500 characters)"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            maxLength={501}
            rows={3}
          />
          {fieldErrors.description && (
            <div className="field-error">{fieldErrors.description}</div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value })}
          >
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {STATUS_LABELS[s]}
              </option>
            ))}
          </select>
          {fieldErrors.status && (
            <div className="field-error">{fieldErrors.status}</div>
          )}
        </div>

        <div className="form-actions">
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting
              ? 'Saving…'
              : editingId
                ? 'Update Task'
                : 'Create Task'}
          </button>
          {editingId && (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={resetForm}
            >
              Cancel
            </button>
          )}
        </div>
      </form>

      {/* Task List */}
      <div className="task-list-header">
        <h2>Tasks</h2>
        <span className="task-count">{tasks.length} task{tasks.length !== 1 ? 's' : ''}</span>
      </div>

      {loading ? (
        <div className="loading">
          <div className="loading-spinner" />
          <p>Loading tasks…</p>
        </div>
      ) : tasks.length === 0 ? (
        <div className="empty-state">
          <p>No tasks yet</p>
          <p className="empty-hint">Create your first task using the form above.</p>
        </div>
      ) : (
        tasks.map((task) => (
          <div key={task.id} className="task-card">
            <div className="task-card-header">
              <span className="task-card-title">{task.title}</span>
              <span className={`status-badge status-${task.status}`}>
                {STATUS_LABELS[task.status]}
              </span>
            </div>
            {task.description && (
              <p className="task-card-description">{task.description}</p>
            )}
            <div className="task-card-footer">
              <span className="task-id">ID: {task.id}</span>
              <div className="task-card-actions">
                <button
                  className="btn btn-edit"
                  onClick={() => handleEdit(task)}
                >
                  Edit
                </button>
                <button
                  className="btn btn-danger"
                  onClick={() => handleDelete(task)}
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        ))
      )}

      <footer className="app-footer">
        © {new Date().getFullYear()} Safaricom Ethiopia. Task Manager — Technical Assessment.
      </footer>
    </div>
  );
}
