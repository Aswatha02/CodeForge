// src/components/Register.js
import React, { useState } from "react";
import { registerUser, saveUser } from "../api";
import { useNavigate } from "react-router-dom";

export default function Register() {
  const [form, setForm] = useState({ username: "", email: "", passwordHash: "" });
  const [err, setErr] = useState(null);
  const nav = useNavigate();

  async function submit(e) {
    e.preventDefault();
    try {
      const res = await registerUser(form);
      saveUser(res);
      nav("/problems");
    } catch (e) {
      setErr(e?.data || e.message);
    }
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <h2>Register</h2>
      {err && <div style={{ color: "red" }}>{JSON.stringify(err)}</div>}
      <form onSubmit={submit}>
        <div>
          <label>Username</label><br />
          <input value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} required />
        </div>
        <div>
          <label>Email</label><br />
          <input value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} type="email" required />
        </div>
        <div>
          <label>Password</label><br />
          <input value={form.passwordHash} onChange={e => setForm({ ...form, passwordHash: e.target.value })} type="password" required />
        </div>
        <button type="submit">Register</button>
      </form>
    </div>
  );
}
