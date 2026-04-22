import React, { useState, useEffect, useRef } from 'react';
import { commentService, activityService, attachmentService, medicalRecordService } from '../services/api';
import { STATUS_MAP } from '../utils/statusHelper';
import { Stethoscope, Lock, FileText, CheckCircle, Upload, Paperclip, Trash2, ExternalLink } from 'lucide-react';

const CaseDetail = ({ caseData, onBack, onGoToPet, currentUserId, userRole }) => {
    const [newMessage, setNewMessage] = useState('');
    const [timeline, setTimeline] = useState([]);
    const [attachments, setAttachments] = useState([]);
    const [isUploading, setIsUploading] = useState(false);
    const [loading, setLoading] = useState(true);

    // States för veterinär-logik och status
    const [showCloseModal, setShowCloseModal] = useState(false);
    const [clinicalNote, setClinicalNote] = useState('');

    const [localStatus, setLocalStatus] = useState(caseData?.status);
    const [isEditing, setIsEditing] = useState(false);
    const [editedDescription, setEditedDescription] = useState(caseData?.description || '');
    const [editedTitle, setEditedTitle] = useState(caseData?.title || '');

    useEffect(() => {
        if (caseData && !isEditing) {
            setLocalStatus(caseData.status);
            setEditedTitle(caseData.title || '');
            setEditedDescription(caseData.description || '');
        }
    }, [caseData, isEditing]);

    const messagesEndRef = useRef(null);
    const statusConfig = STATUS_MAP[localStatus] || { label: localStatus, color: 'bg-slate-50 text-slate-500 border-slate-100' };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (timeline.length > 0) scrollToBottom();
    }, [timeline]);

    useEffect(() => {
        const fetchAllData = async () => {
            if (!caseData?.id) return;
            try {
                setLoading(true);
                const [commentsRes, logsRes, attachRes] = await Promise.all([
                    commentService.getByRecord(caseData.id),
                    activityService.getLogsByRecord(caseData.id),
                    attachmentService.getByRecord(caseData.id)
                ]);

                const combinedTimeline = [
                    ...commentsRes.data.map(c => ({ ...c, type: 'COMMENT' })),
                    ...logsRes.data.map(l => ({ ...l, type: 'ACTIVITY' }))
                ].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

                setTimeline(combinedTimeline);
                setAttachments(attachRes.data);
            } catch (error) {
                console.error("Fel vid hämtning av data:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchAllData();
    }, [caseData?.id]);

    const handleStatusChange = async (newStatus) => {
        try {
            await medicalRecordService.updateStatus(caseData.id, newStatus);
            setLocalStatus(newStatus);
            // Uppdatera loggen direkt efter statusändring
            const logsRes = await activityService.getLogsByRecord(caseData.id);
            setTimeline(prev => [
                ...prev.filter(i => i.type !== 'ACTIVITY'),
                ...logsRes.data.map(l => ({ ...l, type: 'ACTIVITY' }))
            ].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt)));
        } catch (error) {
            alert("Kunde inte uppdatera status.");
        }
    };

    const handleCloseCase = async () => {
        if (!clinicalNote.trim()) return alert("Vänligen skriv en slutnotering.");

        setLoading(true);
        try {
            await medicalRecordService.closeRecord(caseData.id, {
                finalNote: `SLUTGILTIG NOTERING: ${clinicalNote}`
            });

            setShowCloseModal(false);
            onBack();

        } catch (error) {
            console.error("Stängningsfel:", error);
            alert("Kunde inte stänga ärendet. Vänligen kontrollera anslutningen och försök igen.");
        } finally {
            setLoading(false);
        }
    };

    const handleSendMessage = async () => {
        if (!newMessage.trim()) return;
        try {
            const res = await commentService.createComment({
                recordId: caseData.id,
                body: newMessage
            });
            setTimeline(prev => [...prev, { ...res.data, type: 'COMMENT' }]);
            setNewMessage('');
        } catch (error) {
            alert("Kunde inte skicka meddelande.");
        }
    };

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);
        formData.append('description', userRole === 'ROLE_VET' ? 'Kliniskt dokument (Veterinär)' : 'Uppladdad av djurägare');

        try {
            setIsUploading(true);
            const res = await attachmentService.upload(caseData.id, formData);
            setAttachments(prev => [...prev, res.data]);
        } catch (error) {
            alert("Kunde inte ladda upp filen.");
        } finally {
            setIsUploading(false);
        }
    };

    const handleDeleteAttachment = async (id) => {
        if (!window.confirm("Vill du radera bilagan?")) return;
        try {
            await attachmentService.delete(id);
            setAttachments(prev => prev.filter(a => a.id !== id));
        } catch (error) {
            alert("Kunde inte radera filen.");
        }
    };

    const handleSaveRecord = async () => {
        try {
            const res = await medicalRecordService.update(caseData.id, {
                title: editedTitle,
                description: editedDescription
            });

            // Uppdatera lokal data
            caseData.description = res.data.description;
            caseData.title = res.data.title;

            setIsEditing(false);

            const logsRes = await activityService.getLogsByRecord(caseData.id);
            setTimeline(prev => [
                ...prev.filter(i => i.type !== 'ACTIVITY'),
                ...logsRes.data.map(l => ({ ...l, type: 'ACTIVITY' }))
            ].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt)));

        } catch (error) {
            alert("Kunde inte spara ändringarna.");
        }
    };

    if (!caseData || loading) return <div className="p-10 text-center italic text-slate-400">Laddar journal...</div>;

    return (
        <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in slide-in-from-right-4 duration-500 pb-20 text-left">
            {/* NAVIGATION */}
            <button onClick={onBack} className="flex items-center gap-2 text-slate-400 hover:text-vet-navy font-bold text-[10px] uppercase tracking-[0.2em] transition group">
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka till listan
            </button>

            {/* VETERINÄRPANEL */}
            {userRole === 'ROLE_VET' && localStatus !== 'CLOSED' && (
                <div className="bg-slate-900 text-white p-6 rounded-2xl shadow-2xl border border-slate-800">
                    <div className="flex flex-wrap items-center justify-between gap-6 text-left">
                        <div className="flex items-center gap-4">
                            <div className="bg-vet-accent p-3 rounded-xl rotate-3 shadow-lg shadow-vet-accent/20">
                                <Stethoscope size={24} className="text-white" />
                            </div>
                            <div>
                                <h2 className="text-lg font-black italic tracking-tight">Journal</h2>
                                <p className="text-slate-400 text-[10px] font-bold uppercase tracking-[0.1em]">Administration av ärende</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-4">
                            <div className="flex flex-col gap-1">
                                <label className="text-[9px] font-bold text-slate-500 uppercase ml-1 italic">Statuskontroll</label>
                                <select
                                    value={localStatus}
                                    onChange={(e) => handleStatusChange(e.target.value)}
                                    className="bg-slate-800 border border-slate-700 text-sm font-bold rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-vet-accent transition-all cursor-pointer"
                                >
                                    <option value="OPEN">Öppen</option>
                                    <option value="IN_PROGRESS">Under behandling</option>
                                    <option value="AWAITING_TEST_RESULTS">Väntar på provsvar</option>
                                </select>
                            </div>
                            <button
                                onClick={() => setShowCloseModal(true)}
                                className="bg-red-500 hover:bg-red-600 text-white px-6 py-3 rounded-xl text-xs font-black uppercase tracking-widest transition-all shadow-lg shadow-red-500/20 flex items-center gap-2 self-end"
                            >
                                <Lock size={14} /> Stäng Journal
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* HEADER CARD */}
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm text-left">
                <div className="flex items-center gap-5">
                    <div className="bg-vet-navy text-white h-12 w-12 rounded-lg flex items-center justify-center text-xl font-bold shadow-lg shadow-vet-navy/10 italic">
                        {caseData.petName?.charAt(0) || "#"}
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{caseData.title}</h1>
                        <div className="flex items-center gap-3 mt-2">
                            <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border italic ${statusConfig.color}`}>
                                {statusConfig.label}
                            </span>
                            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider italic">Patient: {caseData.petName}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 text-left">
                <div className="lg:col-span-2 space-y-6">
                    {/* DESCRIPTION / JOURNALNOTERING */}
                    <section className="bg-white p-8 rounded-xl border border-slate-200 shadow-sm relative group">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-[10px] font-bold text-vet-accent uppercase tracking-widest italic">
                                Sjukdomshistorik & Medicinsk Journal
                            </h2>

                            {userRole === 'ROLE_VET' && localStatus !== 'CLOSED' && !isEditing && (
                                <button
                                    onClick={() => setIsEditing(true)}
                                    className="text-[10px] font-bold text-slate-400 hover:text-vet-navy uppercase tracking-widest flex items-center gap-1 transition-colors"
                                >
                                    <FileText size={12} /> Redigera Journal
                                </button>
                            )}
                        </div>

                        {isEditing ? (
                            <div className="space-y-4 animate-in fade-in duration-300">
                                <div className="space-y-1">
                                    <label className="text-[9px] font-bold text-slate-400 uppercase ml-1">Rubrik</label>
                                    <input
                                        type="text"
                                        value={editedTitle}
                                        onChange={(e) => setEditedTitle(e.target.value)}
                                        className="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2 text-sm font-bold outline-none focus:border-vet-accent"
                                    />
                                </div>
                                <div className="space-y-1">
                                    <label className="text-[9px] font-bold text-slate-400 uppercase ml-1">Medicinsk beskrivning</label>
                                    <textarea
                                        value={editedDescription}
                                        onChange={(e) => setEditedDescription(e.target.value)}
                                        className="w-full h-64 bg-slate-50 border border-slate-200 rounded-xl p-4 text-sm leading-relaxed outline-none focus:border-vet-accent font-medium italic"
                                    />
                                </div>
                                <div className="flex justify-end gap-3">
                                    <button
                                        onClick={() => { setIsEditing(false); setEditedDescription(caseData.description); }}
                                        className="px-4 py-2 text-[10px] font-bold uppercase tracking-widest text-slate-400 hover:text-slate-600"
                                    >
                                        Avbryt
                                    </button>
                                    <button
                                        onClick={handleSaveRecord}
                                        className="px-6 py-2 bg-vet-navy text-white text-[10px] font-bold uppercase tracking-widest rounded-lg hover:bg-vet-accent transition-all shadow-md"
                                    >
                                        Spara Journalnotering
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="relative">
                                <h3 className="text-lg font-bold text-slate-800 mb-2 italic">{caseData.title}</h3>
                                <p className="text-slate-600 leading-relaxed font-medium italic whitespace-pre-wrap">
                                    {caseData.description}
                                </p>
                            </div>
                        )}
                    </section>

                    {/* ATTACHMENTS */}
                    <section className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest italic">Medicinska Bilagor</h3>
                            <label className={`cursor-pointer bg-vet-navy text-white text-[10px] font-bold px-4 py-2 rounded-lg transition uppercase tracking-widest flex items-center gap-2 ${isUploading ? 'opacity-50 animate-pulse' : 'hover:bg-vet-accent'}`}>
                                {isUploading ? 'Laddar upp...' : <><Upload size={14}/> Ladda upp</>}
                                <input type="file" className="hidden" onChange={handleFileUpload} disabled={isUploading} />
                            </label>
                        </div>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                            {attachments.map((file) => (
                                <div key={file.id} className="group relative bg-slate-50 rounded-xl border border-slate-200 overflow-hidden shadow-sm hover:border-vet-accent transition-colors">
                                    <div className="aspect-square flex items-center justify-center bg-slate-100">
                                        {file.fileType?.includes('image') ?
                                            <img src={file.downloadUrl} alt={file.fileName} className="w-full h-full object-cover" /> :
                                            <FileText className="text-vet-navy opacity-20" size={32} />
                                        }
                                    </div>
                                    <div className="p-2 bg-white border-t border-slate-100">
                                        <p className="text-[9px] font-bold text-slate-700 truncate italic">{file.fileName}</p>
                                        <div className="flex justify-between items-center mt-1">
                                            <a href={file.downloadUrl} target="_blank" rel="noreferrer" className="text-[9px] text-blue-600 font-bold uppercase hover:underline flex items-center gap-1">
                                                <ExternalLink size={10} /> Visa
                                            </a>
                                            <button onClick={() => handleDeleteAttachment(file.id)} className="text-[9px] text-red-400 font-bold uppercase hover:text-red-600 transition-colors">
                                                <Trash2 size={10} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>

                    {/* CONVERSATION */}
                    <section className="space-y-4">
                        <h2 className="text-sm font-bold text-slate-800 ml-1 uppercase tracking-widest italic">Journalförda meddelanden</h2>
                        <div className="bg-slate-50/50 p-4 rounded-2xl border border-slate-100 shadow-inner">
                            <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
                                {timeline.filter(item => item.type === 'COMMENT').map((comment) => (
                                    <div key={comment.id} className={`flex gap-3 items-end ${comment.authorId === currentUserId ? 'flex-row-reverse' : 'flex-row'}`}>
                                        <div className={`h-8 w-8 rounded-lg flex-shrink-0 flex items-center justify-center text-white text-[10px] font-bold shadow-sm italic ${comment.authorId === currentUserId ? 'bg-vet-accent' : 'bg-vet-navy'}`}>
                                            {comment.authorName?.charAt(0)}
                                        </div>
                                        <div className={`p-4 rounded-2xl border shadow-sm max-w-[80%] ${comment.authorId === currentUserId ? 'bg-vet-navy text-white border-vet-navy rounded-br-none' : 'bg-white border-slate-200 text-slate-700 rounded-bl-none'}`}>
                                            <div className={`text-[9px] font-black uppercase tracking-tighter mb-1 opacity-70 ${comment.authorId === currentUserId ? 'text-blue-200' : 'text-vet-accent'}`}>
                                                {comment.authorId === currentUserId ? 'Du' : comment.authorName}
                                            </div>
                                            <p className="text-sm font-medium leading-relaxed italic">{comment.body}</p>
                                        </div>
                                    </div>
                                ))}
                                <div ref={messagesEndRef} />
                            </div>
                        </div>

                        {localStatus !== 'CLOSED' ? (
                            <div className="bg-white p-2 rounded-xl border-2 border-slate-100 shadow-lg flex gap-2 focus-within:border-vet-accent transition-all">
                                <input
                                    type="text"
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                                    placeholder="Skriv en uppdatering eller fråga..."
                                    className="flex-1 bg-transparent px-4 py-3 text-sm outline-none italic font-medium"
                                />
                                <button
                                    onClick={handleSendMessage}
                                    disabled={!newMessage.trim()}
                                    className="bg-vet-navy text-white px-6 py-2 rounded-lg hover:bg-vet-accent transition-all font-bold text-xs uppercase tracking-widest shadow-md disabled:opacity-30"
                                >
                                    Skicka
                                </button>
                            </div>
                        ) : (
                            <div className="bg-slate-100 p-4 rounded-xl text-center border-2 border-dashed border-slate-200">
                                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em] italic flex items-center justify-center gap-2">
                                    <Lock size={12} /> Journalen är arkiverad och kan inte längre ändras
                                </p>
                            </div>
                        )}
                    </section>
                </div>

                {/* RIGHT COLUMN */}
                <div className="space-y-6">
                    <button onClick={() => onGoToPet(caseData.petId)} className="w-full text-left bg-vet-navy p-6 rounded-xl text-white shadow-xl hover:bg-slate-800 transition-all group border border-white/5">
                        <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-4 italic flex justify-between">Patientkort <span className="opacity-0 group-hover:opacity-100 transition-opacity">Visa profil →</span></h3>
                        <div className="flex items-center gap-4">
                            <div className="w-12 h-12 bg-white/10 rounded-lg flex items-center justify-center text-xl font-bold italic">{caseData.petName?.charAt(0) || "?"}</div>
                            <div>
                                <p className="font-bold text-lg italic leading-none group-hover:text-vet-accent">{caseData.petName}</p>
                                <p className="text-xs text-slate-400 mt-1 uppercase font-bold italic">{caseData.petSpecies} • {caseData.clinicName}</p>
                            </div>
                        </div>
                    </button>

                    <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm text-left">
                        <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-6 border-b pb-2 italic">Logg & Historik</h3>
                        <div className="space-y-6 relative before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-100">
                            {timeline.filter(item => item.type === 'ACTIVITY').map((log) => (
                                <div key={log.id} className="relative pl-8">
                                    <div className="absolute left-1.5 top-1.5 w-3 h-3 rounded-full bg-slate-300 border-2 border-white ring-4 ring-slate-50"></div>
                                    <p className="text-[11px] font-bold text-slate-800 italic leading-tight">{log.description}</p>
                                    <p className="text-[9px] text-slate-400 font-bold uppercase mt-1 italic tracking-tighter">{new Date(log.createdAt).toLocaleString('sv-SE')}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            {/* CLOSE CASE MODAL */}
            {showCloseModal && (
                <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
                    <div className="bg-white rounded-[2rem] max-w-lg w-full p-8 shadow-2xl animate-in zoom-in-95 duration-200 text-left">
                        <div className="flex items-center gap-4 mb-6">
                            <div className="bg-red-50 text-red-500 p-3 rounded-2xl">
                                <FileText size={24} />
                            </div>
                            <div>
                                <h3 className="text-xl font-black text-slate-900 italic">Slutför behandling</h3>
                                <p className="text-sm text-slate-500 font-medium italic">Skriv en slutgiltig klinisk notering för att arkivera journalen.</p>
                            </div>
                        </div>

                        <textarea
                            value={clinicalNote}
                            onChange={(e) => setClinicalNote(e.target.value)}
                            placeholder="Sammanfatta diagnos och utförd behandling..."
                            className="w-full h-40 bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 text-sm outline-none focus:border-vet-accent transition-all italic font-medium mb-6"
                        />

                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowCloseModal(false)}
                                className="flex-1 px-6 py-3 border-2 border-slate-100 text-slate-400 text-xs font-black uppercase tracking-widest rounded-xl hover:bg-slate-50 transition-all"
                            >
                                Avbryt
                            </button>
                            <button
                                onClick={handleCloseCase}
                                className="flex-1 px-6 py-3 bg-red-500 text-white text-xs font-black uppercase tracking-widest rounded-xl hover:bg-red-600 shadow-lg shadow-red-500/20 transition-all flex items-center justify-center gap-2"
                            >
                                <CheckCircle size={14} /> Slutför & Stäng
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CaseDetail;