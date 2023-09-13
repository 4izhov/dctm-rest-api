package com.tl.dctm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/*
    id: '1b06ea6080000901',
    stage: (dctm.task_state :: One of: acquired, paused, or finished),
    title: 'Письмо по проекту П3456-891' (dctm.task_name or task_subject),
    author: 'Смит В. (dctm.sent_by)',
    body: '(dctm.message) ТерраЛинк в России – это команда профессионалов. Компания работает в России более 30 лет, обладает опытом реализации сложнейших проектов и входит в ТОП 100 российских ИТ-компаний и список крупнейших поставщиков ИТ-услуг. ТерраЛинк обладает глубокой экспертизой и командой высокопрофессиональных специалистов в области цифровизации и управленческого консалтинга, электронного документооборота, автоматизации бизнес-процессов, управления ресурсами предприятия, кибербезопасности и инфраструктурных решений.',
    date: (dctm.date_sent) 1678446000000,
    level: '(dctm.???) Ok',
    content: [ (список [object_name,r_object_id])
      'content.pdf',
      'Get_Started_With_Smallpdf.pdf',
      'file_sample_100kB.docx',
    ]
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskInfoDto {
    private String id;
    private Integer stage;
    private String title;
    private String author;
    private String body;
    private Long date;
    private String level;
    private Collection<TaskContentInfoDto> content;
}
