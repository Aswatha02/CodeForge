import { useState, useEffect } from "react"

export default function ProblemDetailsTab({ formData, setFormData, categories }) {
  const [selectedCategories, setSelectedCategories] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    if (formData.categoryIds) {
      setSelectedCategories(formData.categoryIds)
    }
  }, [formData.categoryIds])

  useEffect(() => {
    // Simulate loading state
    if (categories !== undefined) {
      setIsLoading(false)
    }
  }, [categories])

  const handleCategoryChange = (categoryId) => {
    const updatedCategories = selectedCategories.includes(categoryId)
      ? selectedCategories.filter(id => id !== categoryId)
      : [...selectedCategories, categoryId]

    setSelectedCategories(updatedCategories)
    setFormData({ ...formData, categoryIds: updatedCategories })
  }

  // Debug: Check what categories we're receiving
  console.log("Categories prop:", categories)
  console.log("Categories type:", typeof categories)
  console.log("Is array?", Array.isArray(categories))

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Basic Information */}
        <div className="space-y-4">
          <h3 className="text-lg font-semibold text-text">Basic Information</h3>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Title *</label>
            <input
              type="text"
              value={formData.title || ""}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              placeholder="Enter problem title"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Slug *</label>
            <input
              type="text"
              value={formData.slug || ""}
              onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              placeholder="url-friendly-slug"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Difficulty *</label>
            <select
              value={formData.difficulty || "EASY"}
              onChange={(e) => setFormData({ ...formData, difficulty: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            >
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Status</label>
            <select
              value={formData.status || "DRAFT"}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            >
              <option value="DRAFT">Draft</option>
              <option value="PUBLISHED">Published</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </div>
        </div>

        {/* Function Signature */}
        <div className="space-y-4">
          <h3 className="text-lg font-semibold text-text">Function Signature</h3>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Function Name</label>
            <input
              type="text"
              value={formData.functionName || ""}
              onChange={(e) => setFormData({ ...formData, functionName: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              placeholder="solve"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Parameters</label>
            <input
              type="text"
              value={formData.parameters || ""}
              onChange={(e) => setFormData({ ...formData, parameters: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              placeholder='[{"name": "nums", "type": "number[]"}, {"name": "target", "type": "number"}]'
            />
            <p className="text-xs text-text-muted mt-1">
              Format: JSON array of objects with name and type properties
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Return Type</label>
            <input
              type="text"
              value={formData.returnType || ""}
              onChange={(e) => setFormData({ ...formData, returnType: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              placeholder="number[][]"
            />
          </div>
        </div>
      </div>

      {/* Description */}
      <div>
        <label className="block text-sm font-medium text-text-muted mb-2">Description *</label>
        <textarea
          value={formData.description || ""}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
          rows="6"
          placeholder="Describe the problem statement..."
        />
      </div>

      {/* Input/Output Format */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Input Format *</label>
          <textarea
            value={formData.inputFormat || ""}
            onChange={(e) => setFormData({ ...formData, inputFormat: e.target.value })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            rows="4"
            placeholder="Describe the input format..."
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Output Format *</label>
          <textarea
            value={formData.outputFormat || ""}
            onChange={(e) => setFormData({ ...formData, outputFormat: e.target.value })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            rows="4"
            placeholder="Describe the output format..."
          />
        </div>
      </div>

      {/* Constraints and Examples */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Constraints</label>
          <textarea
            value={formData.constraints || ""}
            onChange={(e) => setFormData({ ...formData, constraints: e.target.value })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            rows="4"
            placeholder="List the constraints..."
          />
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Points</label>
            <input
              type="number"
              value={formData.points || 100}
              onChange={(e) => setFormData({ ...formData, points: parseInt(e.target.value) || 100 })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              min="0"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-muted mb-2">Tags</label>
            <input
              type="text"
              value={formData.tags || ""}
              onChange={(e) => setFormData({ ...formData, tags: e.target.value })}
              className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
              placeholder="array, two-pointers, sorting"
            />
          </div>
        </div>
      </div>

      {/* Example Input/Output */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Example Input</label>
          <textarea
            value={formData.exampleInput || ""}
            onChange={(e) => setFormData({ ...formData, exampleInput: e.target.value })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            rows="3"
            placeholder="Example input..."
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Example Output</label>
          <textarea
            value={formData.exampleOutput || ""}
            onChange={(e) => setFormData({ ...formData, exampleOutput: e.target.value })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            rows="3"
            placeholder="Example output..."
          />
        </div>
      </div>

      {/* Limits */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Time Limit (ms) *</label>
          <input
            type="number"
            value={formData.timeLimitMs || 2000}
            onChange={(e) => setFormData({ ...formData, timeLimitMs: parseInt(e.target.value) || 2000 })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            min="100"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Memory Limit (MB) *</label>
          <input
            type="number"
            value={formData.memoryLimitMb || 256}
            onChange={(e) => setFormData({ ...formData, memoryLimitMb: parseInt(e.target.value) || 256 })}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
            min="1"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-text-muted mb-2">Is Private</label>
          <div className="flex items-center mt-2">
            <input
              type="checkbox"
              checked={formData.isPrivate || false}
              onChange={(e) => setFormData({ ...formData, isPrivate: e.target.checked })}
              className="mr-2"
            />
            <span className="text-sm text-text-muted">Private problem</span>
          </div>
        </div>
      </div>

      {/* Categories */}
      <div>
        <label className="block text-sm font-medium text-text-muted mb-2">Categories</label>
        {isLoading ? (
          <div className="text-text-muted">Loading categories...</div>
        ) : categories && Array.isArray(categories) && categories.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
            {categories.map((category) => (
              <label key={category.id} className="flex items-center space-x-2 p-2 rounded hover:bg-surface-light">
                <input
                  type="checkbox"
                  checked={selectedCategories.includes(category.id)}
                  onChange={() => handleCategoryChange(category.id)}
                  className="rounded border-border text-primary focus:ring-primary"
                />
                <span className="text-sm text-text">{category.name}</span>
              </label>
            ))}
          </div>
        ) : (
          <div className="text-text-muted p-4 border border-border rounded-md text-center">
            No categories available. Please create some categories first.
          </div>
        )}
      </div>
    </div>
  )
}