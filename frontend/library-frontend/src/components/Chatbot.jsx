import React, { useState, useRef, useEffect } from 'react';
import './chatbot.css';
import { askChatbot } from '../api/chatbotApi';
import { Bot, X, Send, User as UserIcon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Chatbot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { sender: 'bot', text: 'Xin chào! Mình là trợ lý thư viện. Mình có thể giúp gì cho bạn?' }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);
    const { user } = useAuth(); // Only show or rely on auth if needed, but it's good to have.

    // Auto scroll to bottom
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = async (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        const userMsg = input.trim();
        setMessages(prev => [...prev, { sender: 'user', text: userMsg }]);
        setInput('');
        setIsLoading(true);

        try {
            const data = await askChatbot(userMsg);
            setMessages(prev => [...prev, { sender: 'bot', text: data.reply }]);
        } catch (error) {
            console.error('Chat error:', error);
            setMessages(prev => [...prev, { sender: 'bot', text: 'Xin lỗi, hệ thống AI đang bận. Vui lòng thử lại sau.' }]);
        } finally {
            setIsLoading(false);
        }
    };

    // If user is not logged in, maybe we still allow them to ask about books, so we don't hide it.
    // If you want it only for logged-in users: if (!user) return null;

    return (
        <div className={`chatbot-wrapper ${isOpen ? 'open' : ''}`}>
            {isOpen ? (
                <div className="chatbot-window">
                    <div className="chatbot-header">
                        <div className="chatbot-header-info">
                            <Bot className="bot-icon" />
                            <span>Trợ lý Thư viện</span>
                        </div>
                        <button className="chatbot-close" onClick={() => setIsOpen(false)}>
                            <X size={20} />
                        </button>
                    </div>

                    <div className="chatbot-messages">
                        {messages.map((msg, index) => (
                            <div key={index} className={`message-container ${msg.sender}`}>
                                {msg.sender === 'bot' && <Bot className="msg-icon-bot" size={16} />}
                                <div className={`message-bubble ${msg.sender}`}>
                                    {msg.text}
                                </div>
                                {msg.sender === 'user' && <UserIcon className="msg-icon-user" size={16} />}
                            </div>
                        ))}
                        {isLoading && (
                            <div className="message-container bot">
                                <Bot className="msg-icon-bot" size={16} />
                                <div className="message-bubble bot typing">
                                    <span className="dot"></span>
                                    <span className="dot"></span>
                                    <span className="dot"></span>
                                </div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    <form className="chatbot-input-area" onSubmit={handleSend}>
                        <input
                            type="text"
                            placeholder="Nhập câu hỏi... (VD: Còn sách Java không?)"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            disabled={isLoading}
                        />
                        <button type="submit" disabled={isLoading || !input.trim()} className="send-btn">
                            <Send size={18} />
                        </button>
                    </form>
                </div>
            ) : (
                <button className="chatbot-trigger" onClick={() => setIsOpen(true)}>
                    <Bot size={28} />
                </button>
            )}
        </div>
    );
};

export default Chatbot;
