package com.tl.dctm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/*
    id: '1b06ea6080000901',
    stage:  'dormant' (dctm.task_state),
    title:  'Письмо по проекту П3456-891' (dctm.task_name),
    author: 'Смит В. (dctm.sent_by)',
    body:   'ТерраЛинк в России – это команда профессионалов...' (dctm.task_subject),
    date:   1678446000000 (dctm.date_sent),
    level:  'Ok' (расчёт на фронте),
    content: [ (список [object_name,r_object_id])
      {id : '<r_object_id>', name : 'content.pdf'},
      {id : '<r_object_id>', name : 'Get_Started_With_Smallpdf.pdf'},
      {id : '<r_object_id>', name : 'file_sample_100kB.docx'}
    ],
    priority: 'low|medium|high',
    dueDate: 1678446000000 (dctm.due_date)
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskInfoDto {
    private String id;
    private String stage;
    private String title;
    private String author;
    private String body;
    private Long dateSent;
    private String priority;
    private Collection<TaskContentInfoDto> content;
    private Long dueDate;
}
