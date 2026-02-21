import { useState } from "react";
import { useAuth } from "../hooks/UseAuth";
import { updateProfile } from "../services/UserService";
import { useNavigate } from "react-router-dom";
import type { FormEvent } from "react";

export const EditProfilePage = () => {
	const currentUser = useAuth().user;
	const navigate = useNavigate();
	const [username, setUsername] = useState(currentUser?.username || "");
	const [email, setEmail] = useState(currentUser?.email || "");
	const [profilePicture, setProfilePicture] = useState(
		currentUser?.profilePicture || "",
	);
	const [success, setSuccess] = useState(false);
	const [error, setError] = useState<string | null>(null);
	const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
		try {
			e.preventDefault();
			await updateProfile(currentUser?.id || "", {
				username,
				email,
				profilePicture,
			});
			setSuccess(true);
			setTimeout(() => {
				navigate(`/profile/${currentUser?.id}`);
			}, 3000);
		} catch (error) {
			console.error(error);
			setError("Error al actualizar el perfil");
		}
	};

	return (
		<>
			{success && (
				<div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
					Perfil actualizado correctamente
				</div>
			)}
			{error && (
				<div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
					{error}
				</div>
			)}
			<div className="mx-auto max-w-6xl px-4 py-8">
				<h1 className="mb-6 text-3xl font-bold text-gray-900">
					Editar perfil de {currentUser?.username}
				</h1>
				<form onSubmit={handleSubmit}>
					<div className="mb-4">
						<label
							htmlFor="username"
							className="block text-gray-700 font-medium mb-2"
						>
							Nombre de usuario
						</label>
						<input
							type="text"
							id="username"
							value={username}
							onChange={(e) => setUsername(e.target.value)}
							className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
							required
						/>
					</div>
					<div className="mb-4">
						<label
							htmlFor="email"
							className="block text-gray-700 font-medium mb-2"
						>
							Email
						</label>
						<input
							type="email"
							id="email"
							value={email}
							onChange={(e) => setEmail(e.target.value)}
							className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
							required
						/>
					</div>

					<div className="mb-4">
						<label
							htmlFor="profilePicture"
							className="block text-gray-700 font-medium mb-2"
						>
							Imagen de perfil
						</label>
						<input
							type="text"
							id="profilePicture"
							value={profilePicture}
							onChange={(e) => setProfilePicture(e.target.value)}
							className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
							required
						/>
						<img
							src={profilePicture}
							alt="Imagen de perfil"
							className="w-full h-auto"
						/>
					</div>
					<button
						type="submit"
						className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
					>
						Guardar cambios
					</button>
				</form>
			</div>
		</>
	);
};
