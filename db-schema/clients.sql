USE [ingmanXchange]
GO
/****** Object:  Table [dbo].[clients]    Script Date: 03/22/2016 13:29:53 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[clients](
	[code_k] [varchar](13) NULL,		/*код контрагента*/
	[name_k] [varchar](100) NULL,		/*наименование контрагента*/
	[code_mk] [varchar](10) NULL,		/*код менеджера контрагента*/
	[name_mk] [varchar](100) NULL,		/*наименование менеджера контрагента*/
	[code_r] [varchar](8) NULL,		/*код пункта разгрузки*/
	[name_r] [varchar](100) NULL,		/*пункт разгрузки*/
	[code_mr] [varchar](50) NULL,		/*код менеджера разгрузки*/
	[name_mr] [varchar](100) NULL,		/*менеджер разгрузки*/
	[datetime_unload] [datetime] NULL	/*датавремя выгрузки из 1С*/
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF