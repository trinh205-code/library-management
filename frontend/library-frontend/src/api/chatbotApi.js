import axiosInstance from './axiosInstance';

export const askChatbot = async (message) => {
    const response = await axiosInstance.post('/api/chatbot/ask', { message });
    return response.data;
};
