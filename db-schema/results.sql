USE [ingmanXchange]
GO
/****** Object:  Table [dbo].[results]    Script Date: 03/22/2016 13:33:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[results](
	[order_id] [varchar](36) NULL,		/*UID заявки*/
	[description] [varchar](max) NULL,	/*описание результата (произвольный текст)*/
	[datetime_unload] [datetime] NULL	/*датавремя когда запись попала в таблицу из 1С. (датавремя обработки заявки и формирования ответа)*/
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF