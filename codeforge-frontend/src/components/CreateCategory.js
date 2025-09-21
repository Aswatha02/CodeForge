// src/components/CreateCategory.js
import React, { useState } from "react";
import { createCategory } from "../api";
import { useNavigate } from "react-router-dom";

export default function CreateCategory(){
  const [form, setForm] = useState({ name: "", description: "" });
  const [err, setErr] = useState(null);
  const nav = useNavigate();

  async function submit(e){
    e.preventDefault();
    try {
      await createCategory(form);
      nav("/categories");
    } catch (e) {
      setErr(e?.data || e.message);
    }
  }

  return (
    <div>
      <h2>Create Category</h2>
      {err && <div style={{ color: "red" }}>{JSON.stringify(err)}</div>}
      <form onSubmit={submit}>
        <div><label>Name</label><br/>
          <input value={form.name} onChange={e => setForm({...form, name: e.target.value})} required/>
        </div>
        <div><label>Description</label><br/>
          <input value={form.description} onChange={e => setForm({...form, description: e.target.value})}/>
        </div>
        <button type="submit">Create</button>
      </form>
    </div>
  );
}
