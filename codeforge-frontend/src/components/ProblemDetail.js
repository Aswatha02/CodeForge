// src/components/ProblemDetail.js
import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getProblem } from "../api";

export default function ProblemDetail() {
  const { id } = useParams();
  const [problem, setProblem] = useState(null);
  const [err, setErr] = useState(null);

  useEffect(() => {
    getProblem(id).then(setProblem).catch(e => setErr(e?.data || e.message));
  }, [id]);

  if (err) return <div style={{ color: "red" }}>{JSON.stringify(err)}</div>;
  if (!problem) return <div>Loading...</div>;

  return (
    <div>
      <h2>{problem.title}</h2>
      <p><strong>Difficulty:</strong> {problem.difficulty}</p>
      <p>{problem.description}</p>
      <p><strong>Categories:</strong> {problem.categories?.map(c => c.name).join(", ")}</p>
      <hr/>
      <h3>Test cases</h3>
      <ul>
        {(problem.testCases || []).map(tc => (
          <li key={tc.id}>{tc.inputData} → {tc.expectedOutput} {tc.isSample ? "(sample)" : ""}</li>
        ))}
      </ul>
    </div>
  );
}
