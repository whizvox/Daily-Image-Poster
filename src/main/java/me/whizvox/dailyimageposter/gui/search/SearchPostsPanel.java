package me.whizvox.dailyimageposter.gui.search;

import com.github.lgooddatepicker.components.DateTimePicker;
import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.db.Post;
import me.whizvox.dailyimageposter.gui.PostTableModel;
import me.whizvox.dailyimageposter.gui.ViewPostPanel;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class SearchPostsPanel extends JPanel {

  private final SearchPostsFrame parent;
  private final JTextField minNumberField;
  private final JTextField maxNumberField;
  private final JTextField titleField;
  private final JTextField artistField;
  private final JTextField sourceField;
  private final JTextField commentField;
  private final JComboBox<String> hasBeenPostedCombo;
  private final DateTimePicker postedAfterPicker;
  private final DateTimePicker postedBeforePicker;
  private final JComboBox<String> uploadedCombo;
  private final JTable resultsTable;
  private final PostTableModel tableModel;

  private Post selectedPost;

  public SearchPostsPanel(SearchPostsFrame parent) {
    this.parent = parent;
    JLabel minNumberLabel = new JLabel("Min Number");
    minNumberField = new JTextField();
    JLabel maxNumberLabel = new JLabel("Max Number");
    maxNumberField = new JTextField();
    JLabel titleLabel = new JLabel("Title");
    titleField = new JTextField();
    JLabel artistLabel = new JLabel("Artist");
    artistField = new JTextField();
    JLabel sourceLabel = new JLabel("Source");
    sourceField = new JTextField();
    JLabel commentLabel = new JLabel("Comment");
    commentField = new JTextField();
    JLabel hasBeenPostedLabel = new JLabel("Has been posted?");
    hasBeenPostedCombo = new JComboBox<>(new String[] {"Yes or No", "Yes", "No"});
    JLabel postedAfterLabel = new JLabel("Posted after");
    postedAfterPicker = new DateTimePicker();
    JLabel postedBeforeLabel = new JLabel("Posted before");
    postedBeforePicker = new DateTimePicker();
    JLabel uploadedLabel = new JLabel("Image uploaded to");
    uploadedCombo = new JComboBox<>(new String[] {"Imgur or Reddit", "Imgur", "Reddit"});
    JButton searchButton = new JButton("Search");
    tableModel = new PostTableModel();
    resultsTable = new JTable(tableModel);
    resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane tableScrollPane = new JScrollPane(resultsTable);
    JButton viewButton = new JButton("View");
    viewButton.setEnabled(false);

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
            .addComponent(minNumberLabel)
            .addComponent(minNumberField)
            .addComponent(maxNumberLabel)
            .addComponent(maxNumberField)
            .addComponent(titleLabel)
            .addComponent(titleField)
            .addComponent(artistLabel)
            .addComponent(artistField)
            .addComponent(sourceLabel)
            .addComponent(sourceField)
            .addComponent(commentLabel)
            .addComponent(commentField)
            .addComponent(hasBeenPostedLabel)
            .addComponent(hasBeenPostedCombo)
            .addComponent(postedAfterLabel)
            .addComponent(postedAfterPicker)
            .addComponent(postedBeforeLabel)
            .addComponent(postedBeforePicker)
            .addComponent(uploadedLabel)
            .addComponent(uploadedCombo)
            .addComponent(searchButton, GroupLayout.Alignment.CENTER)
        )
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(tableScrollPane)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(viewButton)
            )
        )
    );
    layout.setVerticalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addComponent(minNumberLabel)
            .addComponent(minNumberField)
            .addGap(GAP_SIZE)
            .addComponent(maxNumberLabel)
            .addComponent(maxNumberField)
            .addGap(GAP_SIZE)
            .addComponent(titleLabel)
            .addComponent(titleField)
            .addGap(GAP_SIZE)
            .addComponent(artistLabel)
            .addComponent(artistField)
            .addGap(GAP_SIZE)
            .addComponent(sourceLabel)
            .addComponent(sourceField)
            .addGap(GAP_SIZE)
            .addComponent(commentLabel)
            .addComponent(commentField)
            .addGap(GAP_SIZE)
            .addComponent(hasBeenPostedLabel)
            .addComponent(hasBeenPostedCombo)
            .addGap(GAP_SIZE)
            .addComponent(postedAfterLabel)
            .addComponent(postedAfterPicker)
            .addGap(GAP_SIZE)
            .addComponent(postedBeforeLabel)
            .addComponent(postedBeforePicker)
            .addGap(GAP_SIZE)
            .addComponent(uploadedLabel)
            .addComponent(uploadedCombo)
            .addGap(GAP_SIZE)
            .addComponent(searchButton)
        )
        .addGroup(layout.createSequentialGroup()
            .addComponent(tableScrollPane)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(viewButton)
            )
        )
    );
    setLayout(layout);

    searchButton.addActionListener(event -> {
      String minNumber = minNumberField.getText();
      String maxNumber = maxNumberField.getText();
      String title = titleField.getText();
      String artist = artistField.getText();
      String source = sourceField.getText();
      String comment = commentField.getText();
      int hasBeenPosted = hasBeenPostedCombo.getSelectedIndex();
      LocalDateTime postedAfter = postedAfterPicker.getDateTimePermissive();
      LocalDateTime postedBefore = postedBeforePicker.getDateTimePermissive();
      int uploaded = uploadedCombo.getSelectedIndex();
      Map<String, Object> query = new HashMap<>();
      if (!minNumber.isBlank()) {
        if (maxNumber.isBlank()) {
          query.put("number", minNumber);
        } else {
          query.put("minNumber", minNumber);
        }
      }
      if (!maxNumber.isBlank()) {
        query.put("maxNumber", maxNumber);
      }
      if (!title.isBlank()) {
        query.put("title", title);
      }
      if (!artist.isBlank()) {
        query.put("artist", artist);
      }
      if (!source.isBlank()) {
        query.put("source", source);
      }
      if (!comment.isBlank()) {
        query.put("comment", comment);
      }
      if (hasBeenPosted > 0) {
        query.put("hasBeenPosted", hasBeenPosted == 1);
      }
      if (postedAfter != null) {
        query.put("postedAfter", postedAfter);
      }
      if (postedBefore != null) {
        query.put("postedBefore", postedBefore);
      }
      if (uploaded > 0) {
        query.put("uploaded", uploaded == 1 ? "imgur" : "reddit");
      }
      List<Post> posts = DailyImagePoster.getInstance().posts().search(query).stream()
          .sorted(Comparator.comparingInt(Post::number).thenComparingInt(Post::subNumber))
          .limit(100)
          .toList();
      tableModel.setData(posts);
      selectedPost = null;
      viewButton.setEnabled(false);
    });
    resultsTable.getSelectionModel().addListSelectionListener(e -> {
      selectedPost = tableModel.getPost(((ListSelectionModel) e.getSource()).getLeadSelectionIndex());
      viewButton.setEnabled(true);
      DailyImagePoster.LOG.debug("SELECTED POST: {}", selectedPost);
    });
    viewButton.addActionListener(event -> {
      if (selectedPost != null) {
        JDialog dialog = new JDialog(parent, "View", false);
        dialog.setContentPane(new ViewPostPanel(selectedPost));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
      }
    });
  }

}
