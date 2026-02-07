import { useNavigate } from 'react-router-dom';
import type { Post } from '../types/Post';

interface PostCardProps {
    post: Post;
}

export const PostCard = ({ post }: PostCardProps) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/posts/${post.slug}`);
    };

    const formattedDate = new Date(post.createdAt).toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    });

    return (
        <article
            onClick={handleClick}
            className="cursor-pointer rounded-lg border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md overflow-hidden"
        >
            {post.coverImage ? (
                <img
                    src={post.coverImage}
                    alt={post.title}
                    className="h-48 w-full object-cover"
                />
            ) : (
                <div className="flex h-48 w-full items-center justify-center bg-gray-100">
                    <svg
                        className="h-12 w-12 text-gray-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={1.5}
                            d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.41a2.25 2.25 0 013.182 0l2.909 2.91m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z"
                        />
                    </svg>
                </div>
            )}

            <div className="p-4">
                <h2 className="mb-2 text-xl font-semibold text-gray-900 line-clamp-2">
                    {post.title}
                </h2>

                <p className="mb-3 text-sm text-gray-600 line-clamp-3">
                    {post.summary}
                </p>

                {post.topics.length > 0 && (
                    <div className="mb-3 flex flex-wrap gap-2">
                        {post.topics.map((topic) => (
                            <span
                                key={topic}
                                className="rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800"
                            >
                                {topic}
                            </span>
                        ))}
                    </div>
                )}

                <time className="text-xs text-gray-500" dateTime={post.createdAt}>
                    {formattedDate}
                </time>
            </div>
        </article>
    );
};