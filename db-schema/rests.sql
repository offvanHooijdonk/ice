USE [ingmanXchange]
GO
/****** Object:  Table [dbo].[rests]    Script Date: 03/22/2016 13:32:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[rests](
	[code_s] [varchar](5) NULL,		/*код склада*/
	[name_s] [varchar](60) NULL,		/*склад*/
	[code_p] [varchar](10) NULL,		/* код товара*/
	[name_p] [varchar](100) NULL,		/*товар*/
	[packs] [numeric](18, 3) NULL,		/*остаток упаковок*/
	[amount] [numeric](18, 0) NULL,		/*остаток единиц (штук)*/
	[price] [numeric](18, 2) NULL,		/*цена*/
	[amt_in_pack] [numeric](18, 0) NULL,	/*единиц в упаковке*/
	[gross_weight] [numeric](18, 6) NULL,	/*масса брутто единицы*/
	[barcode] [varchar](15) NULL,		/*штрихкод*/
	[datetime_unload] [datetime] NULL	/*датавремя выгрузки из 1С*/
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF