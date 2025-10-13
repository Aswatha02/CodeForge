// src/components/ProblemList.js
import React, { useEffect, useState } from "react";
import { listProblems } from "../api";
import { Link } from "react-router-dom";

export default function ProblemList() {
  const [problems, setProblems] = useState([]);
  const [err, setErr] = useState(null);

  useEffect(() => {
    listProblems().then(setProblems).catch(e => setErr(e?.data || e.message));
  }, []);

  return (
    <div>
      <h2>Problems</h2>
      <Link to="/problems/new">+ Create Problem</Link>
      {err && <div style={{ color: "red" }}>{JSON.stringify(err)}</div>}
      <ul>
        {problems.map(p => (
          <li key={p.id}>
            <Link to={`/problems/${p.id}`}>{p.title} ({p.difficulty})</Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
