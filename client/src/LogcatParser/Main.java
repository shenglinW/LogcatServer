package LogcatParser;

/*
 * 2013 Fredrik T Lillejordet Released under MIT Licence, see file.
 */

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

public class Main extends JFrame {

  JButton quitButton, startButton, clearButton, stopButton;
  JTextField appField, exceptionField, optionalField, bufferField, serverField, portField;
  JTextArea textArea;
  JCheckBox elevel,wlevel,dlevel,ilevel,vlevel;
  boolean bE,bW,bD,bI,bV;
  ButtonListener listener;
  UpdateText task;
  Socket socket;

  // static int lineNumber = 0;
  int buffer = 0;
  String app = "";
  String exception = "";
  String optional = "";
  String server = "";
  String port = "";

  public Main() {
    initUI();
  }

  public void initUI() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    Container content = new Container();    
    content.setLayout(new FlowLayout());

    content.add(new JLabel("ProcessId:"));
    appField = new JTextField(10);
    content.add(appField);

    content.add(new JLabel("Error"));
    elevel = new JCheckBox();
    elevel.setSelected(true);
    content.add(elevel);

    content.add(new JLabel("Warn"));
    wlevel = new JCheckBox();
    wlevel.setSelected(true);
    content.add(wlevel);
    
    content.add(new JLabel("Info"));
    ilevel = new JCheckBox();
    ilevel.setSelected(true);
    content.add(ilevel);

    content.add(new JLabel("Verbose"));
    vlevel = new JCheckBox();
    vlevel.setSelected(true);
    content.add(vlevel);    
    
    content.add(new JLabel("Debug"));
    dlevel = new JCheckBox();
    dlevel.setSelected(true);
    content.add(dlevel);
    
//    content.add(new JLabel("Exception:"));
//    exceptionField = new JTextField(10);
//    exceptionField.setText("exception");
//    content.add(exceptionField);
//
//    content.add(new JLabel("Optional:"));
//    optionalField = new JTextField(10);
//    content.add(optionalField);
//
//    content.add(new JLabel("Buffer:"));
//    bufferField = new JTextField(3);
//    bufferField.setText("20");
//    content.add(bufferField);
//    
    content.add(new JLabel("Server:"));
    serverField = new JTextField(15);
    serverField.setText("192.168.21.101");
    content.add(serverField);

    content.add(new JLabel("Port:"));
    portField = new JTextField(5);
    portField.setText("1234");
    content.add(portField);
    
    listener = new ButtonListener();
    startButton = new JButton("Start");
    startButton.addActionListener(listener);
    content.add(startButton);

    stopButton = new JButton("Stop");
    stopButton.addActionListener(listener);
    content.add(stopButton);

    clearButton = new JButton("Clear");
    clearButton.addActionListener(listener);
    content.add(clearButton);

    // quitButton = new JButton("Quit");
    // quitButton.addActionListener(listener);
    // content.add(quitButton);
    
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    
        
    textArea = new JTextArea(50,120);
    textArea.setEditable(false);    
    
    DefaultCaret caret = (DefaultCaret) textArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    JScrollPane sp = new JScrollPane(textArea);
    sp.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    content.add(sp);
        
    content.setSize(d.width/2, 100);
    sp.setSize(d.width/2, d.height-100);
    
//    Container container = new Container();    
//    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
//    container.add(content);
//    container.add(sp);
    
//    setLayout(new BorderLayout(5,5)); 
//    getContentPane().add("North", content);
//    getContentPane().add("South", sp);
    
    add(content);
    
    setTitle("Logcat Parser");
    setVisible(true);
//    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(d.width/2, d.height));
    setResizable(false);
    pack();
  }

  private void start() {
    app = appField.getText().toLowerCase();
//    exception = exceptionField.getText().toLowerCase();
//    optional = optionalField.getText().toLowerCase();
    server = serverField.getText().toLowerCase();
    port = portField.getText().toLowerCase();

    appField.setEditable(false);
//    exceptionField.setEditable(false);
//    optionalField.setEditable(false);
//    bufferField.setEditable(false);
    serverField.setEditable(false);
    portField.setEditable(false);
    
    bE  = elevel.isSelected();
    bW  = wlevel.isSelected();
    bD  = dlevel.isSelected();
    bI  = ilevel.isSelected();
    bV  = vlevel.isSelected();

    startButton.setVisible(false);
    (task = new UpdateText()).execute();

  }
  
  private void stop() {
    if (task != null) {
      task.cancel(true);
      task = null;
    }
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e1) {} finally {
        socket = null;
      }
    }
    startButton.setVisible(true);
    appField.setEditable(true);
//    exceptionField.setEditable(true);
//    optionalField.setEditable(true);
//    bufferField.setEditable(true);
    serverField.setEditable(true);
    portField.setEditable(true);
  }

  class UpdateText extends SwingWorker<String, String> {

    @Override
    public String doInBackground() throws Exception {
      if (isCancelled()) {
        return null;
      }
      try {
        Socket tmp = null;
        System.out.println("server " + server + " port:" + port);
        tmp = new Socket(server, Integer.parseInt(port));       
        if (tmp != null) {
          socket = tmp;          
        } else {
          return null;
        }
        
        // Process p = Runtime.getRuntime().exec("adb logcat");
        // BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        System.out.println("socket connected to server " + server);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line = null;
        while ((line = in.readLine()) != null) {
          // System.out.println("line " + line);
          if (line.length() <= 0) continue;
          line = filter(line);
          if (line != null) publish(line);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        stop();
      }
      return null;
    }

    private String filter(String line) {
      String lowercase = line.toLowerCase();

      if (app != null && app.length() > 0) {
        if (!lowercase.contains(app + ")")) return null;
      }

      if (!bD && line.contains("D/")) {
        return null;
      }
      if (!bE && line.contains("E/")) {
        return null;
      }
      if (!bI && line.contains("I/")) {
        return null;
      }
      if (!bW && line.contains("W/")) {
        return null;
      }
      if (!bV && line.contains("V/")) {
        return null;
      }
      return line;
    }

    @Override
    public void process(List<String> chunks) {
      for (String s : chunks) {
        if (textArea.getDocument().getLength() > 0) {
          textArea.append("\n");
        }
        // textArea.append((lineNumber++) + " : " + s);
        textArea.append(s);
      }
      try {
        textArea.setCaretPosition(textArea.getLineStartOffset(textArea.getLineCount() - 1));
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
    }
  }

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == quitButton) {
        System.exit(1);
      } else if (e.getSource() == startButton) {
        start();
      } else if (e.getSource() == clearButton) {
        textArea.setText("");
        // lineNumber = 0;
      } else if (e.getSource() == stopButton) {
        stop();
      }
    }
  }

  public static void main(String[] args) {
    
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    final Main g = new Main();
    g.setSize(d.width/2, d.height);

    /*
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        final Main g = new Main();
        g.setSize(d.width, d.height);

        JButton fullsButton = new JButton("Full Screen");
        fullsButton.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // 通过调用GraphicsEnvironment的getDefaultScreenDevice方法获得当前的屏幕设备了
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            // 全屏设置
            gd.setFullScreenWindow(g);
          }
        });
        g.add(fullsButton);
      }
    });
    */
  }
}
