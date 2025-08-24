/*
 * DlgEvaluasiPRB.java
 * 
 * Form untuk evaluasi pasien PRB berdasarkan kriteria klinis dengan pencarian ICD-10
 */

package simrskhanza;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.ButtonGroup;
import bridging.BPJSProgramPRB;
import bridging.BPJSSuratKontrol;
import javax.swing.SwingUtilities;

public final class DlgEvaluasiPRB extends javax.swing.JDialog {
    private final DefaultTableModel tabMode, tabModeICD;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private WarnaTable warna = new WarnaTable();
    private String dataTensi = ""; // Untuk menyimpan data tensi yang dikirim
    
    private java.awt.Frame parentFrame;
    private static java.util.Stack<javax.swing.JDialog> dialogStack = new java.util.Stack<>();
    
    // Data penyakit PRB dengan mapping ICD-10
    private Map<String, String[]> penyakitICD = new HashMap<>();
    
    private String[] namaPenyakit = {
        "Angina Pektoris",
        "Asma", 
        "Diabetes Mellitus Tipe 2",
        "Epilepsi",
        "Gagal Jantung Kronik",
        "Hipertensi Esensial",
        "Infark Miokard (STEMI/NSTEMI/UAP)",
        "Penyakit Jantung Hipertensi",
        "PPOK",
        "Skizofrenia",
        "Stroke",
        "SLE (Systemic Lupus Erythematosus)"
    };
    
    private String[][] kriteriaKlinis = {
        // Angina Pektoris
        {"Telah dilakukan revaskularisasi (PCI/CABG) sesuai indikasi",
         "Tidak ada sesak nafas atau keluhan nyeri dada saat beraktifitas",
         "Sudah mendapat antiplatelet, beta bloker dosis optimal dan obat golongan statin intensitas tinggi"},
        
        // Asma
        {"Asma stabil dan terkontrol menurut Asthma Control Test ≥20",
         "Tidak ada gejala harian asma lebih dari 2x seminggu",
         "Tidak terbangun pada malam hari akibat sesaknya",
         "Tidak memerlukan obat pelega lebih dari 2x seminggu",
         "Tidak ada keterbatasan aktivitas fisik karena asmanya",
         "Tidak membutuhkan kortikosteroid inhalasi dosis sedang hinggi tinggi",
         "Tidak ada riwayat intubasi dan/atau perawatan ICU karena asma",
         "Tidak ada eksaserbasi berat dalam 12 bulan terakhir"},
        
        // Diabetes Mellitus Tipe 2
        {"GDP 80-130 mg/dl",
         "GD 2PP < 180 mg/dl",
         "HbA1c < 7% (waktu pemeriksaan tiap 6 bulan sekali)",
         "Tekanan darah sistolik < 140 mmHg",
         "Tekanan darah diastolik < 90 mmHg",
         "Kolesterol LDL < 100 (<70 bila risiko KV sangat tinggi)",
         "Kolesterol HDL: Laki-laki > 40, Perempuan > 50",
         "Tidak ada komplikasi akut dan/atau kronik yang masih membutuhkan diagnosis dan tatalaksana lebih lanjut",
         "Insulin tersedia dengan jenis dan cara kerjanya yang sesuai di FKTP",
         "Bukan Diabetes Gestasional dan/atau Diabetes Tipe Lain"},
        
        // Epilepsi
        {"Bebas serangan selama 6 bulan",
         "Telah tercapai dosis rumatan obat anti epilepsy",
         "Tidak ada efek samping obat",
         "Tidak ada komorbid"},
        
        // Gagal Jantung Kronik
        {"Pasien bebas keluhan sesak nafas (NYHA Fc I-II) selama observasi 3 bulan",
         "Volume cairan tubuh tidak bertambah selama 3 bulan berturut-turut",
         "Secara hemodinamik stabil dengan tekanan darah > 90/60 mmHg",
         "Penyebab gagal jantung akut sudah teridentifikasi dan teratasi",
         "Telah mendapatkan terapi gagal jantung sesuai pedoman"},
        
        // Hipertensi Esensial
        {"Tekanan darah < 140/90 mmHg, dalam waktu 3 bulan",
         "Tidak ada penyakit penyerta lain",
         "Tidak terdapat Hypertension Mediated Organ Damaged (HMOD)",
         "Bukan Hipertensi Sekunder, Hipertensi Pulmonal, dan/atau Hipertensi pada kehamilan",
         "Penatalaksanaan Hipertensi derajat 1 (TD 140-150/90-99 mmHg dengan Tahap 1)"},
        
        // Infark Miokard
        {"Telah dilakukan revaskularisasi (PCI/CABG) sesuai indikasi",
         "Klinis pasien stabil dengan gejala nyeri dada/angina maksimal CCS kelas 1 selama evaluasi 3 bulan",
         "Kapasitas fungsional sesuai dengan NYHA I-II",
         "Sudah mendapatkan terapi dual antiplatelet (aspirin dan clopidogrel)",
         "Sudah mendapatkan beta blocker dengan dosis optimal dengan target nadi istirahat < 70 kali permenit",
         "Telah mendapatkan obat golongan statin intensitas tinggi",
         "Telah mendapatkan obat golongan ACE-Inhibitor atau ARB jika EF<40% atau hipertensi",
         "Tekanan Darah Terkontrol TD < 140/90 mmHg"},
        
        // Penyakit Jantung Hipertensi
        {"Tidak ada tanda gagal jantung (kongestif) berupa sesak napas atau nyeri dada dalam observasi selama 3 bulan",
         "Tidak ada aritmia",
         "Tekanan darah terkontrol baik pada waktu istirahat maupun aktivitas (TD< 140/90 mmHg) dalam waktu 3 bulan",
         "Tidak terdapat keterbatasan aktivitas berupa sesak napas dan/atau nyeri dada selama observasi 3 bulan"},
        
        // PPOK
        {"Pasien dengan skor mMRC 0-1 atau skor CAT kurang dari 10",
         "Dalam kurun waktu 1 tahun terakhir tidak ada riwayat eksaserbasi 0 atau 1 kali",
         "Pasien PPOK tidak disertai komorbid yang termasuk ke PRB yang belum tertatalaksana dengan baik",
         "Sudah di follow up dalam bulan dengan kondisi klinis baik tanpa komorbid",
         "Mengerti regimen pengobatan dan teknik inhaler yang digunakan",
         "Mampu melakukan aktivitas fisik untuk kebutuhan hidup sehari-hari"},
        
        // Skizofrenia
        {"PANSS-remisi (P1, P2, P3, N1, N4, N6, G5, G9) semua skor komponen lebih kecil atau sama dengan 3",
         "Tercapai dosis fase rumatan antipsikotik",
         "Skizofrenia tanpa TACC (time, age, comorbidity, complication)",
         "6 bulan tanpa kekambuhan"},
        
        // Stroke
        {"Kondisi pasien stabil walaupun masih didapatkan sekuele stroke infark atau hemoragik",
         "Eksplorasi faktor risiko stroke sudah selesai dilakukan",
         "Tekanan darah kurang dari 130/80 mmHg (bagi yang memiliki riwayat diabetes melitus), dan kurang dari 140/90 mmHg (bagi yang tidak memiliki diabetes melitus)",
         "Gula darah puasa antara 80-130 mg/dL",
         "Kadar kolesterol LDL kurang dari 100 mg/dL (bagi yang memiliki riwayat penyakit Jantung coroner), dan kurang dari 130 mg/dL (bagi yang tidak memiliki riwayat penyakit jantung coroner)",
         "Kadar asam urat kurang dari 6 mg/dL (perempuan) dan kurang dari 7 mg/dL (laki-laki)",
         "Tidak ada faktor risiko lain selain yang disebut di atas"},

        // SLE
        {"Tidak ada manifestasi aktif lupus dalam 6 bulan terakhir",
         "Kadar komplemen C3 dan C4 normal",
         "Anti-dsDNA dalam batas normal atau menurun dari baseline",
         "Tidak ada keterlibatan organ mayor (ginjal, jantung, paru, SSP)",
         "Sudah mendapat terapi imunosupresan yang adekuat",
         "Tidak ada infeksi oportunistik"}
    };

    /** Creates new form DlgEvaluasiPRB 
     *@param parent
     *@param modal*/
    public DlgEvaluasiPRB(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    
    // Debug: Check table structure PERTAMA
    System.out.println("=== MEMULAI DEBUG ===");
    cekStrukturDatabase();
    
    // Initialize ICD mappings
    initializeICDMappings();
    
    // Prevent form from auto-closing
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    
    this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeDialog();
            }
            
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                if (!dialogStack.isEmpty() && dialogStack.peek() == DlgEvaluasiPRB.this) {
                    dialogStack.pop();
                }
            }
        });
    
    // Initialize table model dengan 9 kolom
    tabMode = new DefaultTableModel(null, new Object[]{
        "No.Rawat", "No.RM", "Nama Pasien", "Jenis Penyakit", "Kode ICD-10", "Persentase", "Kesimpulan", "Tanggal Evaluasi", "Keterangan"
    }) {
        @Override
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false;
        }
    };
    tbEvaluasiPRB.setModel(tabMode);

    // Table model untuk pencarian ICD-10
    tabModeICD = new DefaultTableModel(null, new Object[]{
        "Kode ICD-10", "Nama Diagnosis"
    }) {
        @Override
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false;
        }
    };
    tbICD10.setModel(tabModeICD);

    tbEvaluasiPRB.setPreferredScrollableViewportSize(new Dimension(500, 500));
    tbEvaluasiPRB.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // Set column widths untuk 9 kolom
    for (i = 0; i < 9; i++) {
        TableColumn column = tbEvaluasiPRB.getColumnModel().getColumn(i);
        if (i == 0) {
            column.setPreferredWidth(105);
        } else if (i == 1) {
            column.setPreferredWidth(70);
        } else if (i == 2) {
            column.setPreferredWidth(150);
        } else if (i == 3) {
            column.setPreferredWidth(200);
        } else if (i == 4) {
            column.setPreferredWidth(80);
        } else if (i == 5) {
            column.setPreferredWidth(80);
        } else if (i == 6) {
            column.setPreferredWidth(120);
        } else if (i == 7) {
            column.setPreferredWidth(100);
        } else if (i == 8) {
            column.setPreferredWidth(200); // Keterangan
        }
    }
    tbEvaluasiPRB.setDefaultRenderer(Object.class, new WarnaTable());
    tbICD10.setDefaultRenderer(Object.class, new WarnaTable());

    // Input limits
    NoRawat.setDocument(new batasInput((byte) 17).getKata(NoRawat));
    TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
    TCariICD.setDocument(new batasInput((byte) 100).getKata(TCariICD));

    // Document listeners
    if (koneksiDB.CARICEPAT().equals("aktif")) {
        TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (TCari.getText().length() > 2) {
                    tampil();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (TCari.getText().length() > 2) {
                    tampil();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (TCari.getText().length() > 2) {
                    tampil();
                }
            }
        });

        TCariICD.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (TCariICD.getText().length() > 2) {
                    tampilICD();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (TCariICD.getText().length() > 2) {
                    tampilICD();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (TCariICD.getText().length() > 2) {
                    tampilICD();
                }
            }
        });
    }
    
    ChkInput.setSelected(false);
    isForm();
    
    // Initialize buttonGroups
    buttonGroups = new ArrayList<>();
    
    // Populate combo box
    for (String penyakit : namaPenyakit) {
        cmbJenisPenyakit.addItem(penyakit);
    }
    
    // Add listener
    cmbJenisPenyakit.addActionListener(e -> {
        updateKriteriaKlinis();
        tampilICD();
    });

    System.out.println("=== KONSTRUKTOR SELESAI ===");
}

private void closeDialog() {
        try {
            // Cleanup database resources
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connections: " + e);
        }
        
        // Remove from stack
        if (!dialogStack.isEmpty() && dialogStack.peek() == this) {
            dialogStack.pop();
        }
        
        // PERBAIKAN: Proper parent visibility management
        this.setVisible(false);
        
        // Show parent if exists and make it active
        if (parentFrame != null) {
            SwingUtilities.invokeLater(() -> {
                parentFrame.setVisible(true);
                parentFrame.setState(java.awt.Frame.NORMAL); // Restore if minimized
                parentFrame.toFront();
                parentFrame.requestFocus();
            });
        }
        
        this.dispose();
    }

    private void initializeICDMappings() {
        // Angina Pektoris
        penyakitICD.put("Angina Pektoris", new String[]{
            "I20.0", "I20.1", "I20.8", "I20.9"
        });
        
        // Asma
        penyakitICD.put("Asma", new String[]{
            "J45.0", "J45.1", "J45.8", "J45.9"
        });
        
        // Diabetes Mellitus Tipe 2
        penyakitICD.put("Diabetes Mellitus Tipe 2", new String[]{
            "E11.0", "E11.1", "E11.2", "E11.3", "E11.4", "E11.5", 
            "E11.6", "E11.7", "E11.8", "E11.9"
        });
        
        // Epilepsi
        penyakitICD.put("Epilepsi", new String[]{
            "G40.0", "G40.1", "G40.2", "G40.3", "G40.4", "G40.5",
            "G40.6", "G40.7", "G40.8", "G40.9"
        });
        
        // Gagal Jantung Kronik
        penyakitICD.put("Gagal Jantung Kronik", new String[]{
            "I50.0", "I50.1", "I50.2", "I50.3", "I50.4", "I50.9"
        });
        
        // Hipertensi Esensial
        penyakitICD.put("Hipertensi Esensial", new String[]{
            "I10"
        });
        
        // Infark Miokard
        penyakitICD.put("Infark Miokard (STEMI/NSTEMI/UAP)", new String[]{
            "I21.0", "I21.1", "I21.2", "I21.3", "I21.4", "I21.9",
            "I22.0", "I22.1", "I22.2", "I22.8", "I22.9"
        });
        
        // Penyakit Jantung Hipertensi
        penyakitICD.put("Penyakit Jantung Hipertensi", new String[]{
            "I11.0", "I11.9"
        });
        
        // PPOK
        penyakitICD.put("PPOK", new String[]{
            "J44.0", "J44.1", "J44.8", "J44.9"
        });
        
        // Skizofrenia
        penyakitICD.put("Skizofrenia", new String[]{
            "F20.0", "F20.1", "F20.2", "F20.3", "F20.4", "F20.5",
            "F20.6", "F20.8", "F20.9"
        });
        
        // Stroke
        penyakitICD.put("Stroke", new String[]{
            "I60", "I61", "I62", "I63", "I64", "I65", "I66", "I67", "I69"
        });
        
        // SLE
        penyakitICD.put("SLE (Systemic Lupus Erythematosus)", new String[]{
            "M32.0", "M32.1", "M32.8", "M32.9"
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbEvaluasiPRB = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        NoRawat = new widget.TextBox();
        NoRM = new widget.TextBox();
        jLabel3 = new widget.Label();
        NmPasien = new widget.TextBox();
        jLabel4 = new widget.Label();
        jLabel5 = new widget.Label();
        cmbJenisPenyakit = new widget.ComboBox();
        jLabel8 = new widget.Label();
        scrollKriteria = new javax.swing.JScrollPane();
        panelKriteria = new javax.swing.JPanel();
        jLabel9 = new widget.Label();
        lblPersentase = new widget.Label();
        jLabel10 = new widget.Label();
        lblKesimpulan = new widget.Label();
        jLabel11 = new widget.Label();
        txtKodeICD = new widget.TextBox();
        jLabel12 = new widget.Label();
        TCariICD = new widget.TextBox();
        BtnCariICD = new widget.Button();
        ScrollICD = new widget.ScrollPane();
        tbICD10 = new widget.Table();
        jLabel13 = new widget.Label();
        lblTensi = new widget.Label();
        ChkInput = new widget.CekBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Evaluasi Program PRB ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);

        tbEvaluasiPRB.setAutoCreateRowSorter(true);
        tbEvaluasiPRB.setName("tbEvaluasiPRB");
        tbEvaluasiPRB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbEvaluasiPRBMouseClicked(evt);
            }
        });
        tbEvaluasiPRB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbEvaluasiPRBKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbEvaluasiPRB);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3");
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan");
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal");
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus");
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png")));
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit");
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint");
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7");
        jLabel7.setPreferredSize(new java.awt.Dimension(55, 30));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount");
        LCount.setPreferredSize(new java.awt.Dimension(52, 30));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9");
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19");
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"02-11-2021"}));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1");
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21");
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"02-11-2021"}));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2");
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6");
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(290, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('4');
        BtnCari.setToolTipText("Alt+4");
        BtnCari.setName("BtnCari");
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll");
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput");
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(400, 500));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput");
        FormInput.setPreferredSize(new java.awt.Dimension(440, 480));
        FormInput.setLayout(null);

        NoRawat.setEditable(false);
        NoRawat.setHighlighter(null);
        NoRawat.setName("NoRawat");
        FormInput.add(NoRawat);
        NoRawat.setBounds(83, 10, 125, 23);

NoRM.setEditable(false);
NoRM.setHighlighter(null);
NoRM.setName("NoRM");
FormInput.add(NoRM);
NoRM.setBounds(308, 10, 95, 23);  // Move NoRM field to right position

jLabel3.setText("No.Rawat :");
jLabel3.setName("jLabel3");
FormInput.add(jLabel3);
jLabel3.setBounds(0, 10, 80, 23);

// Add proper No.RM label
widget.Label jLabelNoRM = new widget.Label();
jLabelNoRM.setText("No.RM :");
jLabelNoRM.setName("jLabelNoRM");
FormInput.add(jLabelNoRM);
jLabelNoRM.setBounds(218, 10, 90, 23);

NmPasien.setEditable(false);
NmPasien.setHighlighter(null);
NmPasien.setName("NmPasien");
FormInput.add(NmPasien);
NmPasien.setBounds(495, 10, 255, 23);   

jLabel4.setText("Nama Pasien :");
jLabel4.setName("jLabel4");
FormInput.add(jLabel4);
jLabel4.setBounds(405, 10, 90, 23);

        jLabel5.setText("Jenis Penyakit :");
        jLabel5.setName("jLabel5");
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 40, 80, 23);

        cmbJenisPenyakit.setName("cmbJenisPenyakit");
        FormInput.add(cmbJenisPenyakit);
        cmbJenisPenyakit.setBounds(83, 40, 200, 23);

        jLabel11.setText("Kode ICD-10 :");
        jLabel11.setName("jLabel11");
        FormInput.add(jLabel11);
        jLabel11.setBounds(290, 40, 80, 23);

        txtKodeICD.setEditable(false);
        txtKodeICD.setName("txtKodeICD");
        FormInput.add(txtKodeICD);
        txtKodeICD.setBounds(373, 40, 80, 23);

        jLabel13.setText("Tensi :");
        jLabel13.setName("jLabel13");
        FormInput.add(jLabel13);
        jLabel13.setBounds(460, 40, 40, 23);

        lblTensi.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblTensi.setForeground(new java.awt.Color(0, 102, 153));
        lblTensi.setText("-");
        lblTensi.setName("lblTensi");
        FormInput.add(lblTensi);
        lblTensi.setBounds(503, 40, 100, 23);

        jLabel14 = new widget.Label();
    jLabel14.setText("Keterangan :");
    jLabel14.setName("jLabel14");
    FormInput.add(jLabel14);
    jLabel14.setBounds(0, 440, 80, 23);

    txtKeterangan = new widget.TextArea();
    txtKeterangan.setColumns(20);
    txtKeterangan.setRows(3);
    txtKeterangan.setName("txtKeterangan");
    javax.swing.JScrollPane scrollKeterangan = new javax.swing.JScrollPane(txtKeterangan);
    FormInput.add(scrollKeterangan);
    scrollKeterangan.setBounds(83, 440, 655, 60);

    // Update FormInput preferred size:
    FormInput.setPreferredSize(new java.awt.Dimension(440, 520)); // Increase height
    
        jLabel12.setText("Cari ICD-10 :");
        jLabel12.setName("jLabel12");
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 70, 80, 23);

        TCariICD.setName("TCariICD");
        TCariICD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariICDKeyPressed(evt);
            }
        });
        FormInput.add(TCariICD);
        TCariICD.setBounds(83, 70, 200, 23);

        BtnCariICD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCariICD.setMnemonic('5');
        BtnCariICD.setToolTipText("Alt+5");
        BtnCariICD.setName("BtnCariICD");
        BtnCariICD.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariICD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariICDActionPerformed(evt);
            }
        });
        FormInput.add(BtnCariICD);
        BtnCariICD.setBounds(285, 70, 28, 23);

        ScrollICD.setName("ScrollICD");
        ScrollICD.setOpaque(true);

        tbICD10.setName("tbICD10");
        tbICD10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbICD10MouseClicked(evt);
            }
        });
        tbICD10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbICD10KeyPressed(evt);
            }
        });
        ScrollICD.setViewportView(tbICD10);

        FormInput.add(ScrollICD);
        ScrollICD.setBounds(320, 70, 400, 120);

        jLabel8.setText("Kriteria Klinis :");
        jLabel8.setName("jLabel8");
        FormInput.add(jLabel8);
        jLabel8.setBounds(0, 200, 80, 23);

        scrollKriteria.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)));
        scrollKriteria.setName("scrollKriteria");

        panelKriteria.setName("panelKriteria");
        panelKriteria.setLayout(new javax.swing.BoxLayout(panelKriteria, javax.swing.BoxLayout.Y_AXIS));
        scrollKriteria.setViewportView(panelKriteria);

        FormInput.add(scrollKriteria);
        scrollKriteria.setBounds(83, 200, 655, 200);

        jLabel9.setText("Persentase :");
        jLabel9.setName("jLabel9");
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 410, 80, 23);

        lblPersentase.setFont(new java.awt.Font("Tahoma", 1, 12));
        lblPersentase.setForeground(new java.awt.Color(0, 102, 51));
        lblPersentase.setText("0%");
        lblPersentase.setName("lblPersentase");
        FormInput.add(lblPersentase);
        lblPersentase.setBounds(83, 410, 100, 23);

        jLabel10.setText("Kesimpulan :");
        jLabel10.setName("jLabel10");
        FormInput.add(jLabel10);
        jLabel10.setBounds(200, 410, 80, 23);

        lblKesimpulan.setFont(new java.awt.Font("Tahoma", 1, 12));
        lblKesimpulan.setForeground(new java.awt.Color(204, 0, 0));
        lblKesimpulan.setText("Tidak Potensi PRB");
        lblKesimpulan.setName("lblKesimpulan");
        FormInput.add(lblKesimpulan);
        lblKesimpulan.setBounds(283, 410, 200, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png")));
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput");
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png")));
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png")));
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png")));
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>

    // Variable untuk radio button groups
    private ArrayList<ButtonGroup> buttonGroups;

private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {
    int selectedRow = tbEvaluasiPRB.getSelectedRow();
    
    if (tbEvaluasiPRB.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, data sudah habis...!!!!");
        // Ganti dengan komponen yang sesuai untuk request focus, misalnya
        // tbEvaluasiPRB.requestFocus();
    } else if (selectedRow == -1) {
        JOptionPane.showMessageDialog(null, "Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
    } else {
        try {
            // Safely get values with null checks
            Object noRawatObj = tbEvaluasiPRB.getValueAt(selectedRow, 0);
            Object jenisPenyakitObj = tbEvaluasiPRB.getValueAt(selectedRow, 3);
            Object tanggalEvaluasiObj = tbEvaluasiPRB.getValueAt(selectedRow, 7);
            
            if (noRawatObj != null && jenisPenyakitObj != null && tanggalEvaluasiObj != null) {
                if (Sequel.queryu2tf("delete from evaluasi_prb where no_rawat=? and jenis_penyakit=? and tanggal_evaluasi=?", 3, new String[]{
                    noRawatObj.toString(),
                    jenisPenyakitObj.toString(),
                    tanggalEvaluasiObj.toString()
                }) == true) {
                    // Hapus baris dari tabel
                    tabMode.removeRow(selectedRow); // Asumsi tabMode adalah model tabel tbEvaluasiPRB
                    // Update jumlah data jika ada komponen seperti LCount
                    // LCount.setText("" + tabMode.getRowCount()); // Uncomment jika ada LCount
                    tampil();
                    emptTeks();
                } else {
                    JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Data tidak lengkap untuk dihapus");
            }
        } catch (Exception e) {
            System.out.println("Error in delete operation: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error menghapus data: " + e.getMessage());
        }
    }
}

// Method BtnEditActionPerformed yang diperbaiki - tanpa dialog popup
private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {
    if (NoRawat.getText().trim().equals("")) {
        Valid.textKosong(NoRawat, "No.Rawat");
    } else if (cmbJenisPenyakit.getSelectedIndex() == -1) {
        Valid.textKosong(cmbJenisPenyakit, "Jenis Penyakit");
    } else if (txtKodeICD.getText().trim().equals("")) {
        Valid.textKosong(txtKodeICD, "Kode ICD-10");
    } else {
        if (tbEvaluasiPRB.getSelectedRow() != -1) {
            // Hitung persentase dari radio button yang dipilih
            int totalKriteria = getKriteriaCount();
            int kriteriaMemenuhi = getKriteriaMemenuhi();
            
            StringBuilder kriteriaDetail = new StringBuilder();
            for (int i = 0; i < buttonGroups.size(); i++) {
                ButtonGroup bg = buttonGroups.get(i);
                String status = getSelectedRadioValue(bg);
                kriteriaDetail.append("kriteria_").append(i).append(":").append(status);
                if (i < buttonGroups.size() - 1) kriteriaDetail.append("|");
            }
            
            double persentase = totalKriteria > 0 ? (double) kriteriaMemenuhi / totalKriteria * 100 : 0;
            String kesimpulan = persentase >= 50 ? "Potensi PRB" : "Tidak Potensi PRB";
            
            // PERBAIKAN: Gunakan convertRowIndexToModel untuk mendapatkan row yang benar
            int selectedViewRow = tbEvaluasiPRB.getSelectedRow();
            int selectedModelRow = tbEvaluasiPRB.convertRowIndexToModel(selectedViewRow);
            
            try {
                // Ambil data dari model (bukan dari view) untuk menghindari masalah sorting
                String noRawatFromTable = tabMode.getValueAt(selectedModelRow, 0).toString();
                String tanggalEvaluasiFromTable = tabMode.getValueAt(selectedModelRow, 7).toString();
                
                // Siapkan data tensi
                String tensiToSave = dataTensi;
                if (tensiToSave == null || tensiToSave.trim().isEmpty()) {
                    tensiToSave = "-";
                } else if (tensiToSave.length() > 20) {
                    tensiToSave = tensiToSave.substring(0, 20);
                }
                
                // Update database
                String sql = "UPDATE evaluasi_prb SET jenis_penyakit=?, kode_icd10=?, persentase=?, " +
                            "kesimpulan=?, kriteria_detail=?, tensi=?, keterangan=? WHERE no_rawat=? AND tanggal_evaluasi=?";
                
                PreparedStatement ps = koneksi.prepareStatement(sql);
                ps.setString(1, cmbJenisPenyakit.getSelectedItem().toString());
                ps.setString(2, txtKodeICD.getText().trim());
                ps.setString(3, String.format("%.1f", persentase));
                ps.setString(4, kesimpulan);
                ps.setString(5, kriteriaDetail.toString());
                ps.setString(6, tensiToSave);
                ps.setString(7, txtKeterangan.getText().trim());
                ps.setString(8, noRawatFromTable);
                ps.setString(9, tanggalEvaluasiFromTable);
                
                int result = ps.executeUpdate();
                ps.close();
                
                if (result > 0) {
                    // Update data di tabel view (tbEvaluasiPRB) langsung
                    tbEvaluasiPRB.setValueAt(NoRawat.getText().trim(), selectedViewRow, 0);
                    tbEvaluasiPRB.setValueAt(NoRM.getText().trim(), selectedViewRow, 1);
                    tbEvaluasiPRB.setValueAt(NmPasien.getText().trim(), selectedViewRow, 2);
                    tbEvaluasiPRB.setValueAt(cmbJenisPenyakit.getSelectedItem().toString(), selectedViewRow, 3);
                    tbEvaluasiPRB.setValueAt(txtKodeICD.getText().trim(), selectedViewRow, 4);
                    tbEvaluasiPRB.setValueAt(String.format("%.1f%%", persentase), selectedViewRow, 5);
                    tbEvaluasiPRB.setValueAt(kesimpulan, selectedViewRow, 6);
                    tbEvaluasiPRB.setValueAt(tanggalEvaluasiFromTable, selectedViewRow, 7);
                    tbEvaluasiPRB.setValueAt(txtKeterangan.getText().trim(), selectedViewRow, 8);
                    
                    // Update data di model tabel (tabMode) juga
                    tabMode.setValueAt(NoRawat.getText().trim(), selectedModelRow, 0);
                    tabMode.setValueAt(NoRM.getText().trim(), selectedModelRow, 1);
                    tabMode.setValueAt(NmPasien.getText().trim(), selectedModelRow, 2);
                    tabMode.setValueAt(cmbJenisPenyakit.getSelectedItem().toString(), selectedModelRow, 3);
                    tabMode.setValueAt(txtKodeICD.getText().trim(), selectedModelRow, 4);
                    tabMode.setValueAt(String.format("%.1f%%", persentase), selectedModelRow, 5);
                    tabMode.setValueAt(kesimpulan, selectedModelRow, 6);
                    tabMode.setValueAt(tanggalEvaluasiFromTable, selectedModelRow, 7);
                    tabMode.setValueAt(txtKeterangan.getText().trim(), selectedModelRow, 8);
                    
                    // Kosongkan form setelah berhasil update
                    emptTeks();
                } else {
                    JOptionPane.showMessageDialog(null, "Gagal mengupdate data");
                }
                
            } catch (SQLException e) {
                System.out.println("Update Error: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error mengupdate: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Index Error: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error: Data tidak ditemukan pada baris yang dipilih");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih terlebih dulu data yang mau anda ganti...\n Klik data pada table untuk memilih data...!!!!");
        }
    }
}

private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {
    if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
        BtnEditActionPerformed(null);
    } else {
        Valid.pindah(evt, BtnHapus, BtnPrint);
    }
}

    // Event handlers untuk pencarian ICD-10
    private void TCariICDKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariICDActionPerformed(null);
        }
    }

    private void BtnCariICDActionPerformed(java.awt.event.ActionEvent evt) {
        tampilICD();
    }

    private void tbICD10MouseClicked(java.awt.event.MouseEvent evt) {
        if (tabModeICD.getRowCount() != 0) {
            try {
                getDataICD();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }

    private void tbICD10KeyPressed(java.awt.event.KeyEvent evt) {
        if (tabModeICD.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getDataICD();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }

    private void getDataICD() {
        if (tbICD10.getSelectedRow() != -1) {
            txtKodeICD.setText(tbICD10.getValueAt(tbICD10.getSelectedRow(), 0).toString());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgEvaluasiPRB dialog = new DlgEvaluasiPRB(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify
    private widget.TextArea txtKeterangan; // Add this line
    private widget.Label jLabel14; // Add this line
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCariICD;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.Label LCount;
    private widget.TextBox NmPasien;
    private widget.TextBox NoRM;
    private widget.TextBox NoRawat;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane ScrollICD;
    private widget.TextBox TCari;
    private widget.TextBox TCariICD;
    private widget.ComboBox cmbJenisPenyakit;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.Label lblKesimpulan;
    private widget.Label lblPersentase;
    private widget.Label lblTensi;
    private javax.swing.JPanel panelKriteria;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JScrollPane scrollKriteria;
    private widget.Table tbEvaluasiPRB;
    private widget.Table tbICD10;
    private widget.TextBox txtKodeICD;
    // End of variables declaration

    public void tampil() {
    Valid.tabelKosong(tabMode);
    try {
        // Updated SQL query to include keterangan column
        ps = koneksi.prepareStatement("select evaluasi_prb.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, "
                + "evaluasi_prb.jenis_penyakit, evaluasi_prb.kode_icd10, evaluasi_prb.persentase, evaluasi_prb.kesimpulan, "
                + "evaluasi_prb.tanggal_evaluasi, evaluasi_prb.keterangan from evaluasi_prb inner join reg_periksa "
                + "on evaluasi_prb.no_rawat=reg_periksa.no_rawat inner join pasien "
                + "on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where "
                + "evaluasi_prb.tanggal_evaluasi between ? and ? "
                + (TCari.getText().trim().equals("") ? "" : " and (evaluasi_prb.no_rawat like ? or "
                + "reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or "
                + "evaluasi_prb.jenis_penyakit like ?)") + " order by evaluasi_prb.tanggal_evaluasi desc");
        try {
            ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
            if (!TCari.getText().equals("")) {
                ps.setString(3, "%" + TCari.getText().trim() + "%");
                ps.setString(4, "%" + TCari.getText().trim() + "%");
                ps.setString(5, "%" + TCari.getText().trim() + "%");
                ps.setString(6, "%" + TCari.getText().trim() + "%");
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                // Updated to include keterangan column (index 8)
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"), 
                    rs.getString("no_rkm_medis"), 
                    rs.getString("nm_pasien"),
                    rs.getString("jenis_penyakit"), 
                    rs.getString("kode_icd10"), 
                    rs.getString("persentase") + "%", 
                    rs.getString("kesimpulan"), 
                    rs.getString("tanggal_evaluasi"),
                    rs.getString("keterangan") != null ? rs.getString("keterangan") : ""
                });
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
        LCount.setText("" + tabMode.getRowCount());
    } catch (SQLException e) {
        System.out.println("Notifikasi : " + e);
    }
}

    private void tampilICD() {
        Valid.tabelKosong(tabModeICD);
        
        String selectedPenyakit = (String) cmbJenisPenyakit.getSelectedItem();
        if (selectedPenyakit != null && penyakitICD.containsKey(selectedPenyakit)) {
            String[] icdCodes = penyakitICD.get(selectedPenyakit);
            
            try {
                // Build query for searching ICD-10 codes
                StringBuilder query = new StringBuilder();
                query.append("select kd_penyakit, nm_penyakit from penyakit where kd_penyakit in (");
                for (int i = 0; i < icdCodes.length; i++) {
                    query.append("?");
                    if (i < icdCodes.length - 1) {
                        query.append(",");
                    }
                }
                query.append(")");
                
                if (!TCariICD.getText().trim().equals("")) {
                    query.append(" and (kd_penyakit like ? or nm_penyakit like ?)");
                }
                
                query.append(" order by kd_penyakit");
                
                ps = koneksi.prepareStatement(query.toString());
                
                // Set parameters for ICD codes
                for (int i = 0; i < icdCodes.length; i++) {
                    ps.setString(i + 1, icdCodes[i]);
                }
                
                // Set search parameters if search text is not empty
                if (!TCariICD.getText().trim().equals("")) {
                    ps.setString(icdCodes.length + 1, "%" + TCariICD.getText().trim() + "%");
                    ps.setString(icdCodes.length + 2, "%" + TCariICD.getText().trim() + "%");
                }
                
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeICD.addRow(new Object[]{
                        rs.getString("kd_penyakit"),
                        rs.getString("nm_penyakit")
                    });
                }
                
            } catch (Exception e) {
                System.out.println("Error tampil ICD: " + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (ps != null) ps.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e);
                }
            }
        }
    }

// Method getData() yang diperbaiki - menambahkan pengambilan data tensi dari database
private void getData() {
    int selectedViewRow = tbEvaluasiPRB.getSelectedRow();
    
    if (selectedViewRow != -1 && tbEvaluasiPRB.getRowCount() > 0 && tbEvaluasiPRB.getColumnCount() >= 9) {
        try {
            // PERBAIKAN: Konversi view row ke model row
            int selectedModelRow = tbEvaluasiPRB.convertRowIndexToModel(selectedViewRow);
            
            System.out.println("=== DEBUG getData ===");
            System.out.println("Selected View Row: " + selectedViewRow);
            System.out.println("Selected Model Row: " + selectedModelRow);
            
            // Ambil data dari model untuk menghindari masalah dengan sorting
            Object noRawatObj = tabMode.getValueAt(selectedModelRow, 0);
            Object noRMObj = tabMode.getValueAt(selectedModelRow, 1);
            Object nmPasienObj = tabMode.getValueAt(selectedModelRow, 2);
            Object jenisPenyakitObj = tabMode.getValueAt(selectedModelRow, 3);
            Object kodeICDObj = tabMode.getValueAt(selectedModelRow, 4);
            Object tanggalEvaluasiObj = tabMode.getValueAt(selectedModelRow, 7);
            Object keteranganObj = tabMode.getValueAt(selectedModelRow, 8);
            
            NoRawat.setText(noRawatObj != null ? noRawatObj.toString() : "");
            NoRM.setText(noRMObj != null ? noRMObj.toString() : "");
            NmPasien.setText(nmPasienObj != null ? nmPasienObj.toString() : "");
            
            if (jenisPenyakitObj != null) {
                String jenisPenyakit = jenisPenyakitObj.toString();
                cmbJenisPenyakit.setSelectedItem(jenisPenyakit);
            }
            
            txtKodeICD.setText(kodeICDObj != null ? kodeICDObj.toString() : "");
            txtKeterangan.setText(keteranganObj != null ? keteranganObj.toString() : "");
            
            updateKriteriaKlinis();
            tampilICD();
            
            // PERBAIKAN UTAMA: Load kriteria detail dan data tensi dari database
            if (noRawatObj != null && tanggalEvaluasiObj != null) {
                try {
                    // Query untuk mengambil kriteria_detail DAN tensi sekaligus
                    ps = koneksi.prepareStatement("select kriteria_detail, tensi from evaluasi_prb where no_rawat=? and tanggal_evaluasi=?");
                    ps.setString(1, noRawatObj.toString());
                    ps.setString(2, tanggalEvaluasiObj.toString());
                    rs = ps.executeQuery();
                    
                    if (rs.next()) {
                        // Load kriteria detail
                        String kriteriaDetail = rs.getString("kriteria_detail");
                        if (kriteriaDetail != null) {
                            loadKriteriaFromString(kriteriaDetail);
                        }
                        
                        // PERBAIKAN: Load data tensi dari database
                        String tensiFromDB = rs.getString("tensi");
                        if (tensiFromDB != null && !tensiFromDB.trim().isEmpty()) {
                            dataTensi = tensiFromDB.trim();
                            lblTensi.setText(dataTensi);
                            System.out.println("Data tensi loaded from database: '" + dataTensi + "'");
                        } else {
                            // Jika tidak ada data tensi di tabel evaluasi_prb, coba ambil dari tabel pemeriksaan
                            String tensiFromPemeriksaan = getDataTensiFromDB(noRawatObj.toString());
                            if (tensiFromPemeriksaan != null && !tensiFromPemeriksaan.equals("-")) {
                                dataTensi = tensiFromPemeriksaan;
                                lblTensi.setText(dataTensi);
                                System.out.println("Data tensi loaded from pemeriksaan_ralan: '" + dataTensi + "'");
                            } else {
                                dataTensi = "-";
                                lblTensi.setText("-");
                                System.out.println("No tensi data found, set to default");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error loading kriteria and tensi: " + e);
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (ps != null) ps.close();
                    } catch (SQLException e) {
                        System.out.println("Error closing database resources: " + e);
                    }
                }
            }
            
            hitungPersentase();
            
        } catch (Exception e) {
            System.out.println("Error in getData(): " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
}

// Method setNoRm yang diperbaiki untuk pengambilan data tensi
public void setNoRm(String norawat, String norm, String namapasien, String tensi) {
    // DEBUG: Print semua parameter yang diterima
    System.out.println("=== DEBUG setNoRm METHOD ===");
    System.out.println("Parameter 1 (norawat): " + norawat);
    System.out.println("Parameter 2 (norm): " + norm);
    System.out.println("Parameter 3 (namapasien): " + namapasien);
    System.out.println("Parameter 4 (tensi): '" + tensi + "' (Panjang: " + (tensi != null ? tensi.length() : "null") + ")");
    
    // Set field dengan benar
    NoRawat.setText(norawat != null ? norawat : "");
    NoRM.setText(norm != null ? norm : "");
    NmPasien.setText(namapasien != null ? namapasien : "");
    
    // PERBAIKAN: Validasi data tensi yang lebih baik
    if (tensi != null && !tensi.trim().isEmpty()) {
        String cleanTensi = tensi.trim();
        
        // Cek apakah tensi berisi data yang valid
        if (cleanTensi.matches("\\d+/\\d+")) {
            // Format yang benar: "120/80"
            dataTensi = cleanTensi;
            System.out.println("Format tensi valid: " + dataTensi);
        } else if (cleanTensi.matches("\\d+")) {
            // Hanya sistol: "120"
            dataTensi = cleanTensi;
            System.out.println("Hanya sistol: " + dataTensi);
        } else if (!cleanTensi.equals("-") && !cleanTensi.isEmpty()) {
            // Ada data tapi format tidak standar, tetap gunakan
            dataTensi = cleanTensi;
            System.out.println("Format tidak standar tapi ada data: " + dataTensi);
        } else {
            // Data kosong atau "-"
            dataTensi = "-";
            System.out.println("Data tensi kosong atau default");
        }
    } else {
        // Parameter tensi null atau kosong
        dataTensi = "-";
        System.out.println("Parameter tensi null atau kosong");
    }
    
    // Set label tensi
    lblTensi.setText(dataTensi);
    System.out.println("DataTensi yang ditampilkan: '" + dataTensi + "'");
    
    TCari.setText(norawat != null ? norawat : "");
    ChkInput.setSelected(true);
    isForm();
    tampil();
    
    // Auto detect hypertension hanya jika data tensi valid
    if (dataTensi != null && !dataTensi.equals("-") && dataTensi.matches("\\d+/\\d+")) {
        autoDetectHypertension(dataTensi);
    }
    
    System.out.println("=== SELESAI setNoRm ===");
}

// Method tambahan untuk mengambil data tensi dari database jika parameter kosong
private String getDataTensiFromDB(String noRawat) {
    String tensiFromDB = "-";
    
    try {
        // Query untuk mengambil data tensi dari tabel pemeriksaan_ralan
        String sql = "SELECT tensi FROM pemeriksaan_ralan WHERE no_rawat=? ORDER BY tgl_perawatan DESC, jam_rawat DESC LIMIT 1";
        PreparedStatement ps = koneksi.prepareStatement(sql);
        ps.setString(1, noRawat);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            String tensi = rs.getString("tensi");
            if (tensi != null && !tensi.trim().isEmpty()) {
                tensiFromDB = tensi.trim();
                System.out.println("Data tensi dari DB: " + tensiFromDB);
            }
        }
        
        rs.close();
        ps.close();
        
    } catch (SQLException e) {
        System.out.println("Error mengambil data tensi dari DB: " + e.getMessage());
    }
    
    return tensiFromDB;
}

// Method setNoRm yang diperbaiki dengan pengambilan otomatis dari DB
public void setNoRmWithAutoTensi(String norawat, String norm, String namapasien, String tensi) {
    // DEBUG: Print semua parameter yang diterima
    System.out.println("=== DEBUG setNoRmWithAutoTensi METHOD ===");
    System.out.println("Parameter tensi diterima: '" + tensi + "'");
    
    // Set field dasar
    NoRawat.setText(norawat != null ? norawat : "");
    NoRM.setText(norm != null ? norm : "");
    NmPasien.setText(namapasien != null ? namapasien : "");
    
    // Jika tensi kosong atau tidak valid, ambil dari database
    if (tensi == null || tensi.trim().isEmpty() || tensi.equals("-")) {
        System.out.println("Tensi kosong, mengambil dari database...");
        tensi = getDataTensiFromDB(norawat);
    }
    
    // Validasi dan set data tensi
    if (tensi != null && !tensi.trim().isEmpty() && !tensi.equals("-")) {
        String cleanTensi = tensi.trim();
        
        if (cleanTensi.matches("\\d+/\\d+") || cleanTensi.matches("\\d+")) {
            dataTensi = cleanTensi;
            System.out.println("Data tensi valid: " + dataTensi);
        } else {
            dataTensi = cleanTensi; // Tetap gunakan data yang ada
            System.out.println("Data tensi format tidak standar: " + dataTensi);
        }
    } else {
        dataTensi = "-";
        System.out.println("Tidak ada data tensi tersedia");
    }
    
    lblTensi.setText(dataTensi);
    
    TCari.setText(norawat != null ? norawat : "");
    ChkInput.setSelected(true);
    isForm();
    tampil();
    
    // Auto detect hypertension
    if (dataTensi != null && !dataTensi.equals("-") && dataTensi.matches("\\d+/\\d+")) {
        autoDetectHypertension(dataTensi);
    }
    
    System.out.println("=== SELESAI setNoRmWithAutoTensi ===");
}

// Method untuk debugging: cek nama field tensi di tabel pemeriksaan_ralan
public void checkTensiFieldName() {
    try {
        String sql = "SHOW COLUMNS FROM pemeriksaan_ralan LIKE '%tens%'";
        PreparedStatement ps = koneksi.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        System.out.println("=== FIELD TENSI DI TABEL ===");
        while (rs.next()) {
            System.out.println("Field: " + rs.getString("Field") + " | Type: " + rs.getString("Type"));
        }
        System.out.println("============================");
        
        rs.close();
        ps.close();
        
        // Alternatif: cek semua field yang mirip
        sql = "SHOW COLUMNS FROM pemeriksaan_ralan";
        ps = koneksi.prepareStatement(sql);
        rs = ps.executeQuery();
        
        System.out.println("=== SEMUA FIELD DI TABEL PEMERIKSAAN_RALAN ===");
        while (rs.next()) {
            String fieldName = rs.getString("Field");
            if (fieldName.toLowerCase().contains("tens") || 
                fieldName.toLowerCase().contains("td") ||
                fieldName.toLowerCase().contains("blood")) {
                System.out.println("Possible tensi field: " + fieldName + " | Type: " + rs.getString("Type"));
            }
        }
        System.out.println("==============================================");
        
        rs.close();
        ps.close();
        
    } catch (SQLException e) {
        System.out.println("Error checking tensi field: " + e.getMessage());
    }
}

// Method untuk test pengambilan data tensi
public void testGetTensiData(String noRawat) {
    try {
        // Test berbagai kemungkinan nama field
        String[] possibleFields = {"tensi", "td", "tekanan_darah", "blood_pressure", "TTensi"};
        
        for (String field : possibleFields) {
            try {
                String sql = "SELECT " + field + " FROM pemeriksaan_ralan WHERE no_rawat=? ORDER BY tgl_perawatan DESC, jam_rawat DESC LIMIT 1";
                PreparedStatement ps = koneksi.prepareStatement(sql);
                ps.setString(1, noRawat);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String value = rs.getString(field);
                    System.out.println("Field '" + field + "' value: '" + value + "'");
                }
                
                rs.close();
                ps.close();
                
            } catch (SQLException e) {
                System.out.println("Field '" + field + "' tidak ada atau error: " + e.getMessage());
            }
        }
        
    } catch (Exception e) {
        System.out.println("Error testing tensi data: " + e.getMessage());
    }
}

// Method tambahan untuk cek struktur database
public void cekStrukturDatabase() {
    try {
        String sql = "SHOW COLUMNS FROM evaluasi_prb WHERE Field = 'tensi'";
        PreparedStatement ps = koneksi.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            String type = rs.getString("Type");
            System.out.println("=== STRUKTUR KOLOM TENSI ===");
            System.out.println("Tipe kolom tensi: " + type);
            System.out.println("===========================");
        }
        
        rs.close();
        ps.close();
    } catch (Exception e) {
        System.out.println("Error cek struktur database: " + e.getMessage());
    }
}

    private void autoDetectHypertension(String tensi) {
        if (tensi != null && !tensi.trim().equals("") && !tensi.equals("-")) {
            try {
                // Parse blood pressure (format: "120/80")
                String[] parts = tensi.split("/");
                if (parts.length == 2) {
                    int sistolik = Integer.parseInt(parts[0].trim());
                    int diastolik = Integer.parseInt(parts[1].trim());
                    
                    // Check if it matches hypertension criteria (≥140/90)
                    if (sistolik >= 140 || diastolik >= 90) {
                        // Auto select Hipertensi Esensial
                        cmbJenisPenyakit.setSelectedItem("Hipertensi Esensial");
                        updateKriteriaKlinis();
                        tampilICD();
                        
                        // Auto evaluate first criteria (blood pressure control)
                        if (buttonGroups.size() > 0) {
                            ButtonGroup firstGroup = buttonGroups.get(0);
                            // If BP >= 140/90, set first criteria as "Tidak" (not controlled)
                            setRadioButtonValue(firstGroup, "Tidak");
                            hitungPersentase();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing tensi data: " + e);
            }
        }
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 500));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

private void emptTeks() {
    NoRawat.setText("");
    NoRM.setText("");
    NmPasien.setText("");
    cmbJenisPenyakit.setSelectedIndex(-1);
    txtKodeICD.setText("");
    txtKeterangan.setText(""); // Add this line
    lblTensi.setText("-");
    dataTensi = "";
    panelKriteria.removeAll();
    buttonGroups.clear();
    lblPersentase.setText("0%");
    lblKesimpulan.setText("Tidak Potensi PRB");
    lblKesimpulan.setForeground(new java.awt.Color(204, 0, 0));
    panelKriteria.revalidate();
    panelKriteria.repaint();
    Valid.tabelKosong(tabModeICD);
}

    private void updateKriteriaKlinis() {
        panelKriteria.removeAll();
        buttonGroups.clear();
        
        int selectedIndex = cmbJenisPenyakit.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < kriteriaKlinis.length) {
            String[] kriteria = kriteriaKlinis[selectedIndex];
            
            for (int i = 0; i < kriteria.length; i++) {
                // Create panel for each criteria
                javax.swing.JPanel criteriaPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
                
                // Create label for criteria text
                javax.swing.JLabel lblKriteria = new javax.swing.JLabel((i + 1) + ". " + kriteria[i]);
                lblKriteria.setPreferredSize(new java.awt.Dimension(500, 25));
                criteriaPanel.add(lblKriteria);
                
                // Create radio buttons for Ya/Tidak
                javax.swing.JRadioButton radioYa = new javax.swing.JRadioButton("Ya");
                javax.swing.JRadioButton radioTidak = new javax.swing.JRadioButton("Tidak");
                
                // Group radio buttons
                ButtonGroup group = new ButtonGroup();
                group.add(radioYa);
                group.add(radioTidak);
                buttonGroups.add(group);
                
                // Set default selection to "Tidak"
                radioTidak.setSelected(true);
                
                // Add action listeners
                radioYa.addActionListener(e -> hitungPersentase());
                radioTidak.addActionListener(e -> hitungPersentase());
                
                criteriaPanel.add(radioYa);
                criteriaPanel.add(radioTidak);
                
                panelKriteria.add(criteriaPanel);
            }
        }
        
        panelKriteria.revalidate();
        panelKriteria.repaint();
        hitungPersentase();
    }

    private void hitungPersentase() {
        int totalKriteria = buttonGroups.size();
        int kriteriaMemenuhi = getKriteriaMemenuhi();
        
        double persentase = totalKriteria > 0 ? (double) kriteriaMemenuhi / totalKriteria * 100 : 0;
        lblPersentase.setText(String.format("%.1f%%", persentase));
        
        if (persentase >= 50) {
            lblKesimpulan.setText("Potensi PRB");
            lblKesimpulan.setForeground(new java.awt.Color(0, 102, 51));
        } else {
            lblKesimpulan.setText("Tidak Potensi PRB");
            lblKesimpulan.setForeground(new java.awt.Color(204, 0, 0));
        }
    }

    private int getKriteriaCount() {
        return buttonGroups.size();
    }

    private int getKriteriaMemenuhi() {
        int count = 0;
        for (ButtonGroup group : buttonGroups) {
            if (getSelectedRadioValue(group).equals("Ya")) {
                count++;
            }
        }
        return count;
    }

    private String getSelectedRadioValue(ButtonGroup group) {
        for (javax.swing.AbstractButton button : java.util.Collections.list(group.getElements())) {
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return "Tidak"; // default
    }

    private void setRadioButtonValue(ButtonGroup group, String value) {
        for (javax.swing.AbstractButton button : java.util.Collections.list(group.getElements())) {
            if (button.getText().equals(value)) {
                button.setSelected(true);
                break;
            }
        }
    }

    private void loadKriteriaFromString(String kriteriaDetail) {
        if (kriteriaDetail != null && !kriteriaDetail.isEmpty()) {
            String[] items = kriteriaDetail.split("\\|");
            
            for (int i = 0; i < items.length && i < buttonGroups.size(); i++) {
                String[] parts = items[i].split(":");
                if (parts.length == 2) {
                    String value = parts[1];
                    ButtonGroup group = buttonGroups.get(i);
                    setRadioButtonValue(group, value);
                }
            }
        }
    }



    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari, BtnPrint);
        }
    }

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {                                          
        dispose();
    }                                         

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {                                     
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,TCari);}
    }    
    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    BtnCariActionPerformed(evt);
    if (tabMode.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
        TCari.requestFocus();
    } else if (tabMode.getRowCount() != 0) {
        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        Valid.MyReportqry("rptEvaluasiPRB.jasper", "report", "::[ Data Evaluasi PRB ]::",
                "select evaluasi_prb.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, "
                + "evaluasi_prb.jenis_penyakit, evaluasi_prb.kode_icd10, evaluasi_prb.persentase, evaluasi_prb.kesimpulan, "
                + "evaluasi_prb.tanggal_evaluasi, evaluasi_prb.keterangan from evaluasi_prb inner join reg_periksa "
                + "on evaluasi_prb.no_rawat=reg_periksa.no_rawat inner join pasien "
                + "on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where "
                + "evaluasi_prb.tanggal_evaluasi between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' "
                + (TCari.getText().trim().equals("") ? "" : " and (evaluasi_prb.no_rawat like '%" + TCari.getText().trim() + "%' or "
                + "reg_periksa.no_rkm_medis like '%" + TCari.getText().trim() + "%' or "
                + "pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or "
                + "evaluasi_prb.jenis_penyakit like '%" + TCari.getText().trim() + "%')") + " order by evaluasi_prb.tanggal_evaluasi", param);
    }
    this.setCursor(Cursor.getDefaultCursor());
}

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
    }

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        }
    }

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {
        tampil();
    }

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnAll);
        }
    }

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {
        TCari.setText("");
        tampil();
    }

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            tampil();
            TCari.setText("");
        } else {
            Valid.pindah(evt, BtnCari, cmbJenisPenyakit);
        }
    }

    private void tbEvaluasiPRBMouseClicked(java.awt.event.MouseEvent evt) {
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }

    private void tbEvaluasiPRBKeyPressed(java.awt.event.KeyEvent evt) {
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {
        isForm();
    }

// Method BtnSimpanActionPerformed yang diperbaiki
private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {
    // Validasi input dasar
    if (NoRawat.getText().trim().equals("")) {
        Valid.textKosong(NoRawat, "No.Rawat");
        return;
    } else if (cmbJenisPenyakit.getSelectedIndex() == -1) {
        Valid.textKosong(cmbJenisPenyakit, "Jenis Penyakit");
        return;
    } else if (txtKodeICD.getText().trim().equals("")) {
        Valid.textKosong(txtKodeICD, "Kode ICD-10");
        return;
    }
    
    try {
        // Hitung persentase dari radio button yang dipilih
        int totalKriteria = getKriteriaCount();
        int kriteriaMemenuhi = getKriteriaMemenuhi();
        
        // Build kriteria detail string
        StringBuilder kriteriaDetail = new StringBuilder();
        for (int i = 0; i < buttonGroups.size(); i++) {
            ButtonGroup bg = buttonGroups.get(i);
            String status = getSelectedRadioValue(bg);
            kriteriaDetail.append("kriteria_").append(i).append(":").append(status);
            if (i < buttonGroups.size() - 1) kriteriaDetail.append("|");
        }
        
        double persentase = totalKriteria > 0 ? (double) kriteriaMemenuhi / totalKriteria * 100 : 0;
        String kesimpulan = persentase >= 50 ? "Potensi PRB" : "Tidak Potensi PRB";
        String tanggalEvaluasi = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        // Validasi dan siapkan data tensi
        String tensiToSave = dataTensi;
        if (tensiToSave == null || tensiToSave.trim().isEmpty()) {
            tensiToSave = "-";
        } else if (tensiToSave.length() > 20) {
            System.out.println("PERINGATAN: Data tensi terlalu panjang, dipotong dari: " + tensiToSave);
            tensiToSave = tensiToSave.substring(0, 20);
        }
        
        // Debug info
        System.out.println("=== DEBUG SIMPAN DATA ===");
        System.out.println("NoRawat: " + NoRawat.getText().trim());
        System.out.println("NoRM: " + NoRM.getText().trim());
        System.out.println("NmPasien: " + NmPasien.getText().trim());
        System.out.println("JenisPenyakit: " + cmbJenisPenyakit.getSelectedItem().toString());
        System.out.println("KodeICD: " + txtKodeICD.getText().trim());
        System.out.println("Persentase: " + String.format("%.1f", persentase));
        System.out.println("Kesimpulan: " + kesimpulan);
        System.out.println("Tanggal: " + tanggalEvaluasi);
        System.out.println("Tensi: '" + tensiToSave + "'");
        System.out.println("Keterangan: " + txtKeterangan.getText().trim());
        System.out.println("========================");
        
        // Cek apakah data sudah ada (untuk update vs insert)
        boolean isUpdate = false;
        String checkSql = "SELECT COUNT(*) as count FROM evaluasi_prb WHERE no_rawat=? AND jenis_penyakit=?";
        PreparedStatement psCheck = koneksi.prepareStatement(checkSql);
        psCheck.setString(1, NoRawat.getText().trim());
        psCheck.setString(2, cmbJenisPenyakit.getSelectedItem().toString());
        ResultSet rsCheck = psCheck.executeQuery();
        
        if (rsCheck.next() && rsCheck.getInt("count") > 0) {
            isUpdate = true;
        }
        rsCheck.close();
        psCheck.close();
        
        // Pilih query berdasarkan mode
        String sql;
        PreparedStatement ps;
        
        if (isUpdate) {
            // Update existing record
            sql = "UPDATE evaluasi_prb SET persentase=?, kesimpulan=?, kriteria_detail=?, " +
                  "tensi=?, keterangan=?, tanggal_evaluasi=?, kode_icd10=? " +
                  "WHERE no_rawat=? AND jenis_penyakit=?";
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, String.format("%.1f", persentase));
            ps.setString(2, kesimpulan);
            ps.setString(3, kriteriaDetail.toString());
            ps.setString(4, tensiToSave);
            ps.setString(5, txtKeterangan.getText().trim());
            ps.setString(6, tanggalEvaluasi);
            ps.setString(7, txtKodeICD.getText().trim());
            ps.setString(8, NoRawat.getText().trim());
            ps.setString(9, cmbJenisPenyakit.getSelectedItem().toString());
            
            System.out.println("Mode: UPDATE");
        } else {
            // Insert new record
            sql = "INSERT INTO evaluasi_prb (no_rawat, no_rkm_medis, nm_pasien, jenis_penyakit, " +
                  "kode_icd10, persentase, kesimpulan, tanggal_evaluasi, kriteria_detail, tensi, keterangan) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, NoRawat.getText().trim());
            ps.setString(2, NoRM.getText().trim());
            ps.setString(3, NmPasien.getText().trim());
            ps.setString(4, cmbJenisPenyakit.getSelectedItem().toString());
            ps.setString(5, txtKodeICD.getText().trim());
            ps.setString(6, String.format("%.1f", persentase));
            ps.setString(7, kesimpulan);
            ps.setString(8, tanggalEvaluasi);
            ps.setString(9, kriteriaDetail.toString());
            ps.setString(10, tensiToSave);
            ps.setString(11, txtKeterangan.getText().trim());
            
            System.out.println("Mode: INSERT");
        }
        
        // Execute query
        int result = ps.executeUpdate();
        ps.close();
        
        if (result > 0) {
            System.out.println("Data berhasil " + (isUpdate ? "diupdate" : "disimpan") + "!");
            
            // Refresh tabel untuk memastikan data terbaru tampil
            tampil();
            
            // PENTING: Pastikan data telah tersimpan sebelum menampilkan dialog
            // dengan delay kecil untuk memastikan database commit
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Validasi bahwa data benar-benar tersimpan
                    if (validateBeforeShowingDialog()) {
                        showConclusionDialog(persentase);
                    } else {
                        JOptionPane.showMessageDialog(DlgEvaluasiPRB.this,
                            "Data telah disimpan, namun terjadi masalah saat validasi.\n" +
                            "Silakan refresh dan coba lagi jika ingin menggunakan fitur lanjutan.");
                    }
                    
                    // Kosongkan form setelah dialog
                    emptTeks();
                }
            });
            
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data!");
        }
        
    } catch (SQLException e) {
        System.out.println("SQL Error: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("General Error: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        }
    }

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {
        emptTeks();
        ChkInput.setSelected(true);
        isForm();
    }

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnBatalActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
    }

public void checkTableStructure() {
    try {
        PreparedStatement ps = koneksi.prepareStatement("DESCRIBE evaluasi_prb");
        ResultSet rs = ps.executeQuery();
        
        System.out.println("=== TABLE STRUCTURE ===");
        while (rs.next()) {
            System.out.println("Column: " + rs.getString("Field") + 
                             " | Type: " + rs.getString("Type") + 
                             " | Null: " + rs.getString("Null") + 
                             " | Key: " + rs.getString("Key"));
        }
        System.out.println("=======================");
        
        rs.close();
        ps.close();
    } catch (SQLException e) {
        System.out.println("Error checking table structure: " + e.getMessage());
    }
}

// Method showConclusionDialog yang diperbaiki
private void showConclusionDialog(double persentase) {
    String message;
    String title;
    
    if (persentase >= 50) {
        message = String.format("Hasil Evaluasi: %.1f%%\n\nPasien BERPOTENSI PRB!\n\nSilakan pilih tindakan selanjutnya:", persentase);
        title = "Potensi PRB - Tindakan Lanjutan";
    } else {
        message = String.format("Hasil Evaluasi: %.1f%%\n\nPasien TIDAK BERPOTENSI PRB\n\nSilakan pilih tindakan selanjutnya:", persentase);
        title = "Evaluasi Selesai - Tindakan Lanjutan";
    }
    
    // Options yang konsisten untuk kedua skenario
    String[] options = {"Rujukan PRB", "Surat Kontrol", "Selesai"};
    
    int response = JOptionPane.showOptionDialog(this,
        message,
        title,
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        persentase >= 50 ? options[0] : options[1]); // Default berdasarkan hasil
   
    switch (response) {
        case 0: // Rujukan PRB
            System.out.println("User memilih: Rujukan PRB");
            openBPJSProgramPRB();
            break;
            
        case 1: // Surat Kontrol
            System.out.println("User memilih: Surat Kontrol");
            handleSuratKontrolChoice();
            break;
            
        case 2: // Selesai
        default: // User menekan X atau Cancel
            System.out.println("User memilih: Selesai / Cancel");
            // Tidak melakukan apa-apa, dialog tertutup
            break;
    }
}

// Method bantuan untuk validasi sebelum membuka dialog
private boolean validateBeforeShowingDialog() {
    String noRawat = NoRawat.getText().trim();
    
    if (noRawat.isEmpty()) {
        System.out.println("Validasi gagal: NoRawat kosong");
        return false;
    }
    
    // Cek apakah data ada di database
    try {
        String sql = "SELECT COUNT(*) as count FROM evaluasi_prb WHERE no_rawat=?";
        PreparedStatement ps = koneksi.prepareStatement(sql);
        ps.setString(1, noRawat);
        ResultSet rs = ps.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("count");
        }
        rs.close();
        ps.close();
        
        if (count == 0) {
            System.out.println("Validasi gagal: Data tidak ditemukan di database untuk " + noRawat);
            return false;
        }
        
        System.out.println("Validasi berhasil: Ditemukan " + count + " record untuk " + noRawat);
        return true;
        
    } catch (SQLException e) {
        System.out.println("Error validasi: " + e.getMessage());
        return false;
    }
}


// Method handleSuratKontrolChoice yang diperbaiki
private void handleSuratKontrolChoice() {
    String keterangan = JOptionPane.showInputDialog(this,
        "Masukkan keterangan untuk surat kontrol:",
        "Keterangan Surat Kontrol",
        JOptionPane.PLAIN_MESSAGE);
    
    if (keterangan != null && !keterangan.trim().isEmpty()) {
        try {
            String noRawat = "";
            String tanggalEvaluasi = "";
            
            // PERBAIKAN UTAMA: Cek mode berdasarkan apakah ada data yang valid di form
            int selectedViewRow = tbEvaluasiPRB.getSelectedRow();
            
            // Prioritas 1: Jika ada row yang dipilih di tabel DAN data masih valid
            if (selectedViewRow != -1 && selectedViewRow < tbEvaluasiPRB.getRowCount()) {
                try {
                    int selectedModelRow = tbEvaluasiPRB.convertRowIndexToModel(selectedViewRow);
                    
                    if (selectedModelRow >= 0 && selectedModelRow < tabMode.getRowCount()) {
                        noRawat = tabMode.getValueAt(selectedModelRow, 0).toString();
                        tanggalEvaluasi = tabMode.getValueAt(selectedModelRow, 7).toString();
                        
                        System.out.println("=== MODE EDIT - Data dari tabel ===");
                        System.out.println("Selected View Row: " + selectedViewRow);
                        System.out.println("Selected Model Row: " + selectedModelRow);
                        System.out.println("NoRawat dari tabel: '" + noRawat + "'");
                        System.out.println("Tanggal dari tabel: '" + tanggalEvaluasi + "'");
                    } else {
                        System.out.println("Model row index out of bounds, falling back to form data");
                        selectedViewRow = -1; // Force fallback
                    }
                } catch (Exception e) {
                    System.out.println("Error getting data from table, falling back to form: " + e.getMessage());
                    selectedViewRow = -1; // Force fallback
                }
            }
            
            // Prioritas 2: Jika tidak ada row yang dipilih ATAU data tabel tidak valid, ambil dari form
            if (selectedViewRow == -1) {
                noRawat = NoRawat.getText().trim();
                tanggalEvaluasi = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                
                System.out.println("=== MODE BARU/FALLBACK - Data dari form ===");
                System.out.println("NoRawat dari form: '" + noRawat + "'");
                System.out.println("Tanggal: '" + tanggalEvaluasi + "'");
            }
            
            // Validasi data
            if (noRawat.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No Rawat tidak ditemukan!\n" +
                    "Pastikan data sudah disimpan terlebih dahulu atau pilih data dari tabel.");
                return;
            }
            
            // PERBAIKAN: Cek keberadaan data di database dengan lebih fleksibel
            String checkSql = "SELECT no_rawat, tanggal_evaluasi, keterangan FROM evaluasi_prb WHERE no_rawat=? ORDER BY tanggal_evaluasi DESC";
            PreparedStatement psCheck = koneksi.prepareStatement(checkSql);
            psCheck.setString(1, noRawat);
            ResultSet rsCheck = psCheck.executeQuery();
            
            boolean hasData = false;
            String latestDate = "";
            while (rsCheck.next()) {
                hasData = true;
                if (latestDate.isEmpty()) {
                    latestDate = rsCheck.getString("tanggal_evaluasi");
                }
                System.out.println("Found record: " + rsCheck.getString("no_rawat") + 
                                 " | " + rsCheck.getString("tanggal_evaluasi") + 
                                 " | " + rsCheck.getString("keterangan"));
            }
            rsCheck.close();
            psCheck.close();
            
            if (!hasData) {
                JOptionPane.showMessageDialog(this, 
                    "Data evaluasi PRB tidak ditemukan untuk No Rawat: " + noRawat + "\n" +
                    "Silakan simpan data evaluasi terlebih dahulu.");
                return;
            }
            
            // PERBAIKAN: Strategi update yang lebih robust
            boolean updateSuccess = false;
            String updateSql = "";
            PreparedStatement psUpdate = null;
            
            // Strategi 1: Update dengan tanggal spesifik (jika data dari tabel)
            if (selectedViewRow != -1 && !tanggalEvaluasi.isEmpty()) {
                updateSql = "UPDATE evaluasi_prb SET keterangan=? WHERE no_rawat=? AND tanggal_evaluasi=?";
                psUpdate = koneksi.prepareStatement(updateSql);
                psUpdate.setString(1, keterangan.trim());
                psUpdate.setString(2, noRawat);
                psUpdate.setString(3, tanggalEvaluasi);
                
                System.out.println("Strategi 1 - Update spesifik:");
                System.out.println("SQL: " + updateSql);
                System.out.println("Params: '" + keterangan.trim() + "', '" + noRawat + "', '" + tanggalEvaluasi + "'");
                
                int result = psUpdate.executeUpdate();
                psUpdate.close();
                
                System.out.println("Result: " + result);
                updateSuccess = (result > 0);
            }
            
            // Strategi 2: Jika strategi 1 gagal atau tidak applicable, update record terbaru
            if (!updateSuccess && !latestDate.isEmpty()) {
                updateSql = "UPDATE evaluasi_prb SET keterangan=? WHERE no_rawat=? AND tanggal_evaluasi=?";
                psUpdate = koneksi.prepareStatement(updateSql);
                psUpdate.setString(1, keterangan.trim());
                psUpdate.setString(2, noRawat);
                psUpdate.setString(3, latestDate);
                
                System.out.println("Strategi 2 - Update record terbaru:");
                System.out.println("SQL: " + updateSql);
                System.out.println("Params: '" + keterangan.trim() + "', '" + noRawat + "', '" + latestDate + "'");
                
                int result = psUpdate.executeUpdate();
                psUpdate.close();
                
                System.out.println("Result: " + result);
                updateSuccess = (result > 0);
            }
            
            // Strategi 3: Update semua record dengan no_rawat ini (last resort)
            if (!updateSuccess) {
                updateSql = "UPDATE evaluasi_prb SET keterangan=? WHERE no_rawat=?";
                psUpdate = koneksi.prepareStatement(updateSql);
                psUpdate.setString(1, keterangan.trim());
                psUpdate.setString(2, noRawat);
                
                System.out.println("Strategi 3 - Update semua record:");
                System.out.println("SQL: " + updateSql);
                System.out.println("Params: '" + keterangan.trim() + "', '" + noRawat + "'");
                
                int result = psUpdate.executeUpdate();
                psUpdate.close();
                
                System.out.println("Result: " + result);
                updateSuccess = (result > 0);
            }
            
            // Hasil akhir
            if (updateSuccess) {
                txtKeterangan.setText(keterangan.trim());
                JOptionPane.showMessageDialog(this, "Keterangan berhasil disimpan!");
                tampil(); // Refresh tabel
                
                // Buka form surat kontrol
                openBPJSSuratKontrol();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menyimpan keterangan!\n" +
                    "No Rawat: " + noRawat + "\n" +
                    "Silakan coba lagi atau hubungi administrator.");
            }
            
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error database: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("General error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    } else if (keterangan != null) {
        // Jika user memasukkan string kosong
        JOptionPane.showMessageDialog(this, "Keterangan tidak boleh kosong untuk surat kontrol!");
        handleSuratKontrolChoice(); // Recursively call again
    }
    // Jika keterangan == null, berarti user menekan Cancel, tidak perlu action
}

// Method tambahan untuk debugging database
private void debugDatabaseContent(String noRawat) {
    try {
        String sql = "SELECT no_rawat, tanggal_evaluasi, keterangan FROM evaluasi_prb WHERE no_rawat=?";
        PreparedStatement ps = koneksi.prepareStatement(sql);
        ps.setString(1, noRawat);
        ResultSet rs = ps.executeQuery();
        
        System.out.println("=== DEBUG DATABASE CONTENT ===");
        System.out.println("Query: " + sql);
        System.out.println("Parameter: " + noRawat);
        
        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            System.out.println("Record found:");
            System.out.println("  - no_rawat: '" + rs.getString("no_rawat") + "'");
            System.out.println("  - tanggal_evaluasi: '" + rs.getString("tanggal_evaluasi") + "'");
            System.out.println("  - keterangan: '" + rs.getString("keterangan") + "'");
        }
        
        if (!hasData) {
            System.out.println("No records found for no_rawat: '" + noRawat + "'");
        }
        
        rs.close();
        ps.close();
        System.out.println("===============================");
        
    } catch (SQLException e) {
        System.out.println("Error debugging database: " + e.getMessage());
    }
}

// Method untuk validasi dan debug lengkap
private void fullDatabaseDebug(String noRawat, String tanggalEvaluasi) {
    System.out.println("\n=== FULL DATABASE DEBUG ===");
    System.out.println("Input parameters:");
    System.out.println("  - noRawat: '" + noRawat + "' (length: " + noRawat.length() + ")");
    System.out.println("  - tanggalEvaluasi: '" + tanggalEvaluasi + "' (length: " + tanggalEvaluasi.length() + ")");
    
    try {
        // 1. Cek semua record untuk no_rawat ini
        String sql1 = "SELECT COUNT(*) as total FROM evaluasi_prb WHERE no_rawat=?";
        PreparedStatement ps1 = koneksi.prepareStatement(sql1);
        ps1.setString(1, noRawat);
        ResultSet rs1 = ps1.executeQuery();
        
        if (rs1.next()) {
            System.out.println("Total records for this no_rawat: " + rs1.getInt("total"));
        }
        rs1.close();
        ps1.close();
        
        // 2. Cek record spesifik dengan tanggal
        String sql2 = "SELECT COUNT(*) as specific FROM evaluasi_prb WHERE no_rawat=? AND tanggal_evaluasi=?";
        PreparedStatement ps2 = koneksi.prepareStatement(sql2);
        ps2.setString(1, noRawat);
        ps2.setString(2, tanggalEvaluasi);
        ResultSet rs2 = ps2.executeQuery();
        
        if (rs2.next()) {
            System.out.println("Specific records with exact date: " + rs2.getInt("specific"));
        }
        rs2.close();
        ps2.close();
        
        // 3. Tampilkan semua tanggal yang ada
        String sql3 = "SELECT DISTINCT tanggal_evaluasi FROM evaluasi_prb WHERE no_rawat=?";
        PreparedStatement ps3 = koneksi.prepareStatement(sql3);
        ps3.setString(1, noRawat);
        ResultSet rs3 = ps3.executeQuery();
        
        System.out.println("Available dates for this no_rawat:");
        while (rs3.next()) {
            String dbDate = rs3.getString("tanggal_evaluasi");
            System.out.println("  - '" + dbDate + "' (equals input: " + dbDate.equals(tanggalEvaluasi) + ")");
        }
        rs3.close();
        ps3.close();
        
        // 4. Test update query
        System.out.println("Testing UPDATE query preparation...");
        String testSql = "UPDATE evaluasi_prb SET keterangan='TEST' WHERE no_rawat=? AND tanggal_evaluasi=?";
        PreparedStatement psTest = koneksi.prepareStatement(testSql);
        psTest.setString(1, noRawat);
        psTest.setString(2, tanggalEvaluasi);
        System.out.println("UPDATE query prepared successfully");
        psTest.close();
        
    } catch (SQLException e) {
        System.out.println("Debug error: " + e.getMessage());
    }
    
    System.out.println("===========================\n");
}

// Method tambahan untuk validasi yang lebih spesifik
private boolean isEditMode() {
    return tbEvaluasiPRB.getSelectedRow() != -1;
}

// Method untuk mendapatkan data dari row yang dipilih
private String getSelectedRowData(int columnIndex) {
    int selectedRow = tbEvaluasiPRB.getSelectedRow();
    if (selectedRow != -1 && tbEvaluasiPRB.getRowCount() > 0) {
        Object value = tbEvaluasiPRB.getValueAt(selectedRow, columnIndex);
        return value != null ? value.toString() : "";
    }
    return "";
}


// Method fallback untuk update keterangan
private boolean updateKeteranganFallback(String noRawat, String keterangan) {
    try {
        // Update record terakhir berdasarkan no_rawat saja
        String sql = "UPDATE evaluasi_prb SET keterangan=? WHERE no_rawat=? ORDER BY tanggal_evaluasi DESC LIMIT 1";
        
        // Karena MySQL tidak mendukung ORDER BY dengan LIMIT dalam UPDATE, gunakan subquery
        sql = "UPDATE evaluasi_prb SET keterangan=? WHERE no_rawat=? AND tanggal_evaluasi = " +
              "(SELECT max_date FROM (SELECT MAX(tanggal_evaluasi) as max_date FROM evaluasi_prb WHERE no_rawat=?) as temp)";
        
        PreparedStatement ps = koneksi.prepareStatement(sql);
        ps.setString(1, keterangan);
        ps.setString(2, noRawat);
        ps.setString(3, noRawat);
        
        int result = ps.executeUpdate();
        ps.close();
        
        System.out.println("Fallback update result: " + result);
        return result > 0;
        
    } catch (SQLException e) {
        System.out.println("Error in fallback update: " + e.getMessage());
        
        // Last resort: update semua record dengan no_rawat yang sama
        try {
            String simpleSql = "UPDATE evaluasi_prb SET keterangan=? WHERE no_rawat=?";
            PreparedStatement ps = koneksi.prepareStatement(simpleSql);
            ps.setString(1, keterangan);
            ps.setString(2, noRawat);
            
            int result = ps.executeUpdate();
            ps.close();
            
            System.out.println("Last resort update result: " + result);
            return result > 0;
            
        } catch (SQLException ex) {
            System.out.println("Last resort update also failed: " + ex.getMessage());
            return false;
        }
    }
}

// Method untuk memastikan ComboBox tidak null saat digunakan
private void ensureComboBoxNotNull() {
    if (cmbJenisPenyakit.getSelectedItem() == null) {
        if (cmbJenisPenyakit.getItemCount() > 0) {
            cmbJenisPenyakit.setSelectedIndex(0);
        }
    }
}

// Method untuk validasi form sebelum operasi surat kontrol
private boolean validateFormForSuratKontrol() {
    String noRawat = NoRawat.getText().trim();
    
    if (noRawat.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No Rawat tidak boleh kosong!");
        return false;
    }
    
    // Cek apakah ada data di database
    try {
        String checkSql = "SELECT COUNT(*) as count FROM evaluasi_prb WHERE no_rawat=?";
        PreparedStatement ps = koneksi.prepareStatement(checkSql);
        ps.setString(1, noRawat);
        ResultSet rs = ps.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("count");
        }
        rs.close();
        ps.close();
        
        if (count == 0) {
            JOptionPane.showMessageDialog(this, 
                "Tidak ada data evaluasi untuk No Rawat: " + noRawat + "\n" +
                "Silakan simpan data evaluasi terlebih dahulu.");
            return false;
        }
        
        return true;
        
    } catch (SQLException e) {
        System.out.println("Error validating form: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Error validasi: " + e.getMessage());
        return false;
    }
}

private void openBPJSProgramPRB() {
        try {
            // Ambil data yang diperlukan
            String noRawat = NoRawat.getText().trim();
            String noSep = getNoSEPFromDatabase(noRawat);
            String noKartu = getNoKartuFromDatabase(noRawat);
            String noRM = NoRM.getText().trim();
            String namaPasien = NmPasien.getText().trim();
            String alamat = getAlamatFromDatabase(noRawat);
            String email = getEmailFromDatabase(noRawat);
            String kodeDPJP = getDokterDPJPFromDatabase(noRawat);
            String namaDPJP = getNamaDokterDPJPFromDatabase(kodeDPJP);
            
            // STRATEGI 1: CLOSE CURRENT DIALOG FIRST (RECOMMENDED)
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Hide current dialog
                        DlgEvaluasiPRB.this.setVisible(false);
                        
                        // Create new dialog dengan PARENT ASLI (bukan this)
                        BPJSProgramPRB bpjsPRB = new BPJSProgramPRB(parentFrame, true);
                        
                        // Set data
                        bpjsPRB.setNoRm(noRawat, noSep, noKartu, noRM, namaPasien, alamat, email, kodeDPJP, namaDPJP);
                        
                        bpjsPRB.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
                        bpjsPRB.setLocationRelativeTo(parentFrame);
                        
                        // Add listener untuk kembali ke current dialog jika diperlukan
                        bpjsPRB.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosed(java.awt.event.WindowEvent e) {
                                // OPTIONAL: Show current dialog again if needed
                                // DlgEvaluasiPRB.this.setVisible(true);
                                // OR: Show parent
                                if (parentFrame != null) {
                                    parentFrame.setVisible(true);
                                    parentFrame.toFront();
                                }
                            }
                        });
                        
                        bpjsPRB.setVisible(true);
                        
                        // Dispose current dialog to free up resources
                        DlgEvaluasiPRB.this.dispose();
                        
                    } catch (Exception e) {
                        System.out.println("Error opening BPJSProgramPRB: " + e);
                        JOptionPane.showMessageDialog(parentFrame,
                            "Tidak dapat membuka form rujukan PRB: " + e.getMessage());
                        
                        // Show current dialog back if error
                        DlgEvaluasiPRB.this.setVisible(true);
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Error opening BPJSProgramPRB: " + e);
            JOptionPane.showMessageDialog(this, "Tidak dapat membuka form rujukan PRB: " + e.getMessage());
        }
    }


private void openBPJSSuratKontrol() {
    // Validasi data seperti di contoh
    if(tabModekasir.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, table masih kosong...!!!!");
        TCari.requestFocus();
        return;
    }
    
    if(TNoRw.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih dulu dengan menklik data pada table...!!!");
        tbKasirRalan.requestFocus();
        return;
    }
    
    if(tbKasirRalan.getSelectedRow() == -1) {
        JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih dulu dengan menklik data pada table...!!!");
        tbKasirRalan.requestFocus();
        return;
    }
    
    try {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PreparedStatement pskasir = null;
                ResultSet rskasir = null;
                
                try {
                    // Query data seperti di contoh
                    pskasir = koneksi.prepareStatement(
                        "select bridging_sep.no_sep,bridging_sep.no_kartu,bridging_sep.tanggal_lahir," +
                        "bridging_sep.jkel,bridging_sep.nmdiagnosaawal from bridging_sep " +
                        "where bridging_sep.no_rawat=?"
                    );
                    
                    pskasir.setString(1, TNoRw.getText());
                    rskasir = pskasir.executeQuery();
                    
                    if(rskasir.next()) {
                        // Hide current dialog
                        DlgEvaluasiPRB.this.setVisible(false);
                        DlgEvaluasiPRB.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        
                        // Create BPJSSuratKontrol dengan data
                        BPJSSuratKontrol suratKontrol = new BPJSSuratKontrol(parentFrame, true);
                        
                        // Set data seperti di contoh
                        suratKontrol.setNoRm(
                            TNoRwCari.getText(),           // No Rawat
                            rskasir.getString("no_sep"),   // No SEP
                            rskasir.getString("no_kartu"), // No Kartu
                            TNoRMCari.getText(),           // No RM
                            TPasienCari.getText(),         // Nama Pasien
                            rskasir.getString("tanggal_lahir"), // Tanggal Lahir
                            rskasir.getString("jkel"),     // Jenis Kelamin
                            rskasir.getString("nmdiagnosaawal") // Diagnosis Awal
                        );
                        
                        suratKontrol.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
                        suratKontrol.setLocationRelativeTo(parentFrame);
                        
                        // Add listener untuk handle close
                        suratKontrol.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosed(java.awt.event.WindowEvent e) {
                                if (parentFrame != null) {
                                    parentFrame.setVisible(true);
                                    parentFrame.toFront();
                                }
                                DlgEvaluasiPRB.this.setCursor(Cursor.getDefaultCursor());
                            }
                        });
                        
                        suratKontrol.setVisible(true);
                        
                        // Dispose current dialog
                        DlgEvaluasiPRB.this.dispose();
                        
                    } else {
                        // Data SEP tidak ditemukan
                        JOptionPane.showMessageDialog(parentFrame, 
                            "Pasien tersebut belum terbit SEP, silahkan hubungi bagian terkait..!!");
                        TCari.requestFocus();
                        DlgEvaluasiPRB.this.setVisible(true);
                    }
                    
                } catch (SQLException e) {
                    System.out.println("Database Error: " + e.getMessage());
                    JOptionPane.showMessageDialog(parentFrame,
                        "Error mengambil data SEP: " + e.getMessage());
                    DlgEvaluasiPRB.this.setVisible(true);
                    DlgEvaluasiPRB.this.setCursor(Cursor.getDefaultCursor());
                    
                } catch (Exception e) {
                    System.out.println("Error opening BPJSSuratKontrol: " + e.getMessage());
                    JOptionPane.showMessageDialog(parentFrame,
                        "Tidak dapat membuka form surat kontrol: " + e.getMessage());
                    DlgEvaluasiPRB.this.setVisible(true);
                    DlgEvaluasiPRB.this.setCursor(Cursor.getDefaultCursor());
                    
                } finally {
                    // Cleanup resources
                    try {
                        if(rskasir != null) {
                            rskasir.close();
                        }
                        if(pskasir != null) {
                            pskasir.close();
                        }
                    } catch (SQLException e) {
                        System.out.println("Error closing resources: " + e.getMessage());
                    }
                }
            }
        });
        
    } catch (Exception e) {
        System.out.println("Error opening BPJSSuratKontrol: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Tidak dapat membuka form surat kontrol: " + e.getMessage());
    }
}

//METHOD HELPER UNTUK MENGAMBIL DATA YANG DIPERLUKAN
    private String getNoSEPFromDatabase(String noRawat) {
        String noSep = "";
        try {
            String sql = "SELECT no_sep FROM bridging_sep WHERE no_rawat=? ORDER BY tglsep DESC LIMIT 1";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                noSep = rs.getString("no_sep");
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.out.println("Error mengambil no SEP: " + e.getMessage());
        }
        return noSep != null ? noSep : "";
    }

    private String getNoKartuFromDatabase(String noRawat) {
        String noKartu = "";
        try {
            String sql = "SELECT no_kartu FROM bridging_sep WHERE no_rawat=? ORDER BY tglsep DESC LIMIT 1";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                noKartu = rs.getString("no_kartu");
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.out.println("Error mengambil no kartu: " + e.getMessage());
        }
        return noKartu != null ? noKartu : "";
    }

    private String getAlamatFromDatabase(String noRawat) {
        String alamat = "";
        try {
            // Coba dari pasien dulu
            String sql = "SELECT pasien.alamat FROM reg_periksa " +
                        "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                        "WHERE reg_periksa.no_rawat=?";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                alamat = rs.getString("alamat");
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.out.println("Error mengambil alamat: " + e.getMessage());
        }
        return alamat != null ? alamat : "";
    }

    private String getEmailFromDatabase(String noRawat) {
        String email = "";
        try {
            // Coba dari pasien dulu, jika ada kolom email
            String sql = "SELECT pasien.email FROM reg_periksa " +
                        "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                        "WHERE reg_periksa.no_rawat=?";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                email = rs.getString("email");
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.out.println("Error mengambil email (mungkin kolom tidak ada): " + e.getMessage());
            // Jika tidak ada kolom email, return empty string
        }
        return email != null ? email : "";
    }

    private String getDokterDPJPFromDatabase(String noRawat) {
        String kodeDokter = "";
        try {
            String sql = "SELECT kd_dokter FROM reg_periksa WHERE no_rawat=?";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                kodeDokter = rs.getString("kd_dokter");
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.out.println("Error mengambil kode dokter: " + e.getMessage());
        }
        return kodeDokter != null ? kodeDokter : "";
    }

    private String getNamaDokterDPJPFromDatabase(String kodeDokter) {
        String namaDokter = "";
        if (kodeDokter.isEmpty()) return namaDokter;
        
        try {
            String sql = "SELECT nm_dokter FROM dokter WHERE kd_dokter=?";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, kodeDokter);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                namaDokter = rs.getString("nm_dokter");
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.out.println("Error mengambil nama dokter: " + e.getMessage());
        }
        return namaDokter != null ? namaDokter : "";
    }
}