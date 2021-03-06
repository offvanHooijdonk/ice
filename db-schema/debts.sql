USE [ingmanXchange]
GO
/****** Object:  Table [dbo].[debts]    Script Date: 03/22/2016 13:31:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[debts](
	[code_k] [varchar](13) NULL,		/*код контрагента*/
	[name_k] [varchar](100) NULL,		/*контрагент*/
	[code_mk] [varchar](10) NULL,		/*код менеджера контрагента*/
	[rating] [varchar](100) NULL,		/*тип отношений с контрагентом*/
	[debt] [numeric](18, 2) NULL,		/*задолженность*/
	[overdue] [numeric](18, 2) NULL,	/*в т.ч. просроченная задолженность*/
	[datetime_unload] [datetime] NULL	/*датавремя выгрузки из 1С*/
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF