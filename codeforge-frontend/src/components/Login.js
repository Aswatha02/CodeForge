// src/components/Login.js
import React, { useState } from "react";
import { loginUser, saveUser } from "../api";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const [usernameOrEmail, setUsernameOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState(null);
  const nav = useNavigate();

  async function submit(e) {
    e.preventDefault();
    try {
      const user = await loginUser(usernameOrEmail, password);
      saveUser(user);
      nav("/problems");
      window.location.reload();
    } catch (e) {
      setErr(e?.data || e.message);
    }
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <h2>Login</h2>
      {err && <div style={{ color: "red" }}>{JSON.stringify(err)}</div>}
      <form onSubmit={submit}>
        <div>
          <label>Username or Email</label><br />
          <input value={usernameOrEmail} onChange={e => setUsernameOrEmail(e.target.value)} required />
        </div>
        <div>
          <label>Password</label><br />
          <input value={password} onChange={e => setPassword(e.target.value)} type="password" required />
        </div>
        <button type="submit">Login</button>
      </form>
    </div>
  );
}
