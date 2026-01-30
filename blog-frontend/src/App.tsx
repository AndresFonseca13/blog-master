
function App() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-xl shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Project Initialized! 🚀
        </h1>
        <p className="text-gray-600 mb-6">
          React + Vite + TypeScript + Tailwind CSS
        </p>
        <div className="space-y-4">
          <div className="p-4 bg-blue-50 text-blue-700 rounded-lg">
            <p className="font-semibold">Tailwind is working</p>
            <p className="text-sm">This box is styled with utility classes.</p>
          </div>
          <button className="w-full bg-indigo-600 text-white font-medium py-2 px-4 rounded-lg hover:bg-indigo-700 transition-colors">
            Get Started
          </button>
        </div>
      </div>
    </div>
  )
}

export default App
