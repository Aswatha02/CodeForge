// src/components/CategoryList.js
import React, { useEffect, useState } from "react";
import { listCategories } from "../api";
import { Link } from "react-router-dom";

export default function CategoryList(){
  const [cats, setCats] = useState([]);
  useEffect(()=>{ listCategories().then(setCats).catch(console.error) }, []);
  return (
    <div>
      <h2>Categories</h2>
      <Link to="/categories/new">+ Create Category</Link>
      <ul>{cats.map(c => <li key={c.id}>{c.name} — {c.description}</li>)}</ul>
    </div>
  );
}
