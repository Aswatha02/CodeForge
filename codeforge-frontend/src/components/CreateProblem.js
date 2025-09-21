// src/components/CreateProblem.js
import React, { useState, useEffect } from "react";
import { createProblem, listCategories, getUser } from "../api";
import { useNavigate } from "react-router-dom";

export default function CreateProblem() {
  const [form, setForm] = useState({ title: "", slug: "", description: "", difficulty: "EASY", categories: [] });
  const [cats, setCats] = useState([]);
  const [err, setErr] = useState(null);
  const nav = useNavigate();
  const user = getUser();

  useEffect(() => {
    listCategories().then(setCats).catch(e => setErr(e?.data || e.message));
  }, []);

  async function submit(e) {
    e.preventDefault();
    try {
      // Convert selected category IDs to objects with id (backend expects Category objects)
      const selectedCats = cats
        .filter(c => form.categories.includes(c.id))
        .map(c => ({ id: c.id }));

      const payload = { 
        title: form.title,
        slug: form.slug,
        description: form.description,
        difficulty: form.difficulty,
        categories: selectedCats
      };
      const res = await createProblem(payload, user.id);
      nav(`/problems/${res.id}`);
    } catch (e) {
      setErr(e?.data || e.message);
    }
  }

  function toggleCategory(id) {
    setForm(prev => ({
      ...prev,
      categories: prev.categories.includes(id) ? prev.categories.filter(x=>x!==id) : [...prev.categories, id]
    }));
  }

  return (
    <div style={{ maxWidth: 800 }}>
      <h2>Create Problem</h2>
      {err && <div style={{ color: "red" }}>{JSON.stringify(err)}</div>}
      <form onSubmit={submit}>
        <div>
          <label>Title</label><br/>
          <input value={form.title} onChange={e => setForm({...form, title: e.target.value})} required />
        </div>
        <div>
          <label>Slug</label><br/>
          <input value={form.slug} onChange={e => setForm({...form, slug: e.target.value})} required />
        </div>
        <div>
          <label>Description</label><br/>
          <textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})} required />
        </div>
        <div>
          <label>Difficulty</label><br/>
          <select value={form.difficulty} onChange={e => setForm({...form, difficulty: e.target.value})}>
            <option value="EASY">EASY</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HARD">HARD</option>
          </select>
        </div>
        <div>
          <label>Categories</label><br/>
          {cats.map(c => (
            <label key={c.id} style={{ marginRight: 8 }}>
              <input type="checkbox" checked={form.categories.includes(c.id)} onChange={() => toggleCategory(c.id)} /> {c.name}
            </label>
          ))}
        </div>
        <button type="submit">Create</button>
      </form>
    </div>
  );
}
