USE [ingmanXchange]
GO
/****** Object:  Table [dbo].[orders]    Script Date: 03/22/2016 13:32:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[orders](
	[order_id] [varchar](36) NULL,		/*UID заявки*/
	[name_m] [varchar](100) NULL,		/*менеджер*/
	[order_date] [datetime] NULL,		/*дата заявки (какой датой будет оформлен документ в 1С)*/
	[is_advertising] [int] NULL,		/*признак рекламы (0,1)*/
	[code_k] [varchar](13) NULL,		/*код контрагента*/
	[name_k] [varchar](100) NULL,		/*контрагент*/
	[code_r] [varchar](8) NULL,		/*код разгрузки*/
	[name_r] [varchar](100) NULL,		/*пункт разгрузки*/
	[code_s] [varchar](5) NULL,		/*код склада*/
	[name_s] [varchar](60) NULL,		/*имя склада*/
	[code_p] [varchar](50) NULL,		/*код товара*/
	[name_p] [varchar](100) NULL,		/*товар*/
	[amt_packs] [numeric](18, 3) NULL,	/*кол-во упаковок товара*/
	[amount] [numeric](18, 0) NULL,		/*кол-во единиц товара*/
	[comments] [varchar](200) NULL,		/*комментарий к заявке*/
	[in_datetime] [datetime] NULL,		/*датавремя попадания записи в базу MSSQL*/
	[proc_datetime] [datetime] NULL,	/*датавремя когда заявка была обработана роботом*/
	[iddoc] [char](9) NULL			/*мое служебное поле - внутренний идентификатор сформированной заявки в 1С*/
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'0 - false, 1 - true, null - false' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'orders', @level2type=N'COLUMN',@level2name=N'is_advertising'