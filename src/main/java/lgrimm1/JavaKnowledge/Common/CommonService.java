package lgrimm1.JavaKnowledge.Common;

import lgrimm1.JavaKnowledge.Html.*;
import lgrimm1.JavaKnowledge.Process.*;
import lgrimm1.JavaKnowledge.Title.*;
import lgrimm1.JavaKnowledge.Txt.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.lang.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.servlet.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Service
public class CommonService {

	private final TitleRepository titleRepository;
	private final TxtRepository txtRepository;
	private final HtmlRepository htmlRepository;
	private final Formulas formulas;
	private final ProcessRecords processRecords;
	private final FileOperations fileOperations;
	private final Extractors extractors;
	private final ProcessPage processPage;
	private final HtmlGenerators htmlGenerators;

	@Autowired
	public CommonService(TitleRepository titleRepository,
						 TxtRepository txtRepository,
						 HtmlRepository htmlRepository,
						 Formulas formulas,
						 ProcessRecords processRecords,
						 FileOperations fileOperations,
						 Extractors extractors,
						 ProcessPage processPage,
						 HtmlGenerators htmlGenerators) {
		this.titleRepository = titleRepository;
		this.txtRepository = txtRepository;
		this.htmlRepository = htmlRepository;
		this.formulas = formulas;
		this.processRecords = processRecords;
		this.fileOperations = fileOperations;
		this.extractors = extractors;
		this.processPage = processPage;
		this.htmlGenerators = htmlGenerators;
	}

	public ModelAndView getRoot(String initialView) {
		Payload payload = new Payload(
				null,
				null,
				null,
				null,
				null,
				null,
				"",
				null,
				null,
				null
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	/**
	 * Finds titles which contain any word of the given text, furthermore
	 * titles of which their TXT content contains the whole given text.
	 * The search trims the given text and ignores case.
	 * In case there is no search text given, returns all titles.
	 */
	public ModelAndView searchPages(String initialView, Payload payload) {
		if (payload == null || payload.getSearchText() == null || payload.getSearchText().isBlank()) {
			Payload payload2 = new Payload(
					null,
					null,
					null,
					null,
					null,
					null,
					"<all titles>",
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView(initialView, "payload", payload2);
		}
		List<String> titles = processRecords.searchBySearchText(
						payload.getSearchText(),
						titleRepository,
						txtRepository)
				.stream()
				.sorted()
				.toList();
		Payload payload2 = new Payload(
				null,
				null,
				null,
				null,
				null,
				null,
				payload.getSearchText(),
				null,
				null,
				titles
		);
		return new ModelAndView(initialView, "payload", payload2);
	}

	public ModelAndView getPage(String initialView, List<String> titles) {
		if (titles == null || titles.size() != 1 || titles.get(0) == null || titles.get(0).isBlank()) {
			Payload payload2 = new Payload(
					null,
					null,
					null,
					null,
					null,
					null,
					"<all titles>",
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView("list", "payload", payload2);
		}
		Optional<TitleEntity> optionalTitleEntity = titleRepository.findByTitle(titles.get(0));
		if (optionalTitleEntity.isEmpty()) {
			Payload payload2 = new Payload(
					null,
					null,
					null,
					null,
					null,
					null,
					"<all titles>",
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView("list", "payload", payload2);
		}
		Optional<HtmlEntity> optionalHtmlEntity = htmlRepository.findById(optionalTitleEntity.get().getHtmlId());
		if (optionalHtmlEntity.isEmpty()) {
			Payload payload2 = new Payload(
					null,
					null,
					null,
					null,
					null,
					null,
					"<all titles>",
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView("list", "payload", payload2);
		}
		Payload payload2 = new Payload(
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				optionalTitleEntity.get().getFilename() + ".html",
				null,
				optionalHtmlEntity.get().getTitleReferences()
		);
		return new ModelAndView(initialView, "payload", payload2);
	}

	public ModelAndView managePages(String initialView) {
		Payload payload = new Payload(
				false,
				null,
				null,
				null,
				"",
				"",
				null,
				null,
				null,
				processRecords.getAllTitles(titleRepository)
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView createSourcePage(String initialView) {
		Payload payload = new Payload(
				null,
				new ArrayList<>(),
				false,
				"",
				null,
				"",
				null,
				null,
				"",
				null
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView editSourcePage(String initialView, List<String> titles) {
		if (titles == null || titles.size() != 1 || titles.get(0) == null || titles.get(0).isBlank()) {
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					"Please select exactly one title for editing.",
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			System.out.println("======================================\n1st entrance" + payload);
			return new ModelAndView("management", "payload", payload);
		}
		Optional<TitleEntity> optionalTitleEntity = titleRepository.findByTitle(titles.get(0));
		if (optionalTitleEntity.isEmpty()) {
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					"Please select exactly one title for editing.",
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			System.out.println("======================================\n2nd entrance" + payload);
			return new ModelAndView("management", "payload", payload);
		}
		long txtId = optionalTitleEntity.get().getTxtId();
		Optional<TxtEntity> optionalTxtEntity = txtRepository.findById(txtId);
		if (optionalTxtEntity.isEmpty()) {
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					"Please select exactly one title for editing.",
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			System.out.println("======================================\n3rd entrance" + payload);
			return new ModelAndView("management", "payload", payload);
		}
		Payload payload = new Payload(
				null,
				processRecords.stringToList(optionalTxtEntity.get().getContent()),
				true,
				optionalTitleEntity.get().getFilename(),
				null,
				"",
				null,
				null,
				optionalTitleEntity.get().getTitle(),
				null
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView deletePages(String initialView, List<String> titles, Boolean confirm) {
		if (titles == null || titles.size() == 0 || !confirm) {
			String message = !confirm ? "Please confirm deletion." : "Please select titles you wish to delete.";
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					message,
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView(initialView, "payload", payload);
		}
		titles = titles.stream()
				.filter(title -> title != null && !title.isBlank())
				.toList();
		if (titles.isEmpty()) {
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					"Please select existing titles you wish to delete.",
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView(initialView, "payload", payload);
		}
		long numberOfGivenTitles = titles.size();
		String message = processRecords.deleteByTitles(
				titles,
				titleRepository,
				txtRepository,
				htmlRepository) +
				" of " + numberOfGivenTitles + " titles were deleted.";
		Payload payload = new Payload(
				false,
				null,
				null,
				null,
				"",
				message,
				null,
				null,
				null,
				processRecords.getAllTitles(titleRepository)
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView publishPages(String initialView) {
		long[] publishResults = processRecords.publishHtml(titleRepository, htmlRepository, fileOperations);
		Payload payload = new Payload(
				false,
				null,
				null,
				null,
				"",
				publishResults[0] + " HTML files were pre-deleted, " + publishResults[1] + " were published.",
				null,
				null,
				null,
				processRecords.getAllTitles(titleRepository)
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView addFormula(String initialView,
								   String formulaName,
								   String title,
								   String fileName,
								   List<String> content,
								   Boolean editExistingPage) {
		if (title == null || title.isBlank()) {
			title = "";
		}
		if (fileName == null || fileName.isBlank()) {
			fileName = "";
		}
		if (content == null) {
			content = new ArrayList<>();
		}
		if (editExistingPage == null) {
			editExistingPage = false;
		}
		String formula = formulas.getFormula(formulaName);
		String message;
		if (formula.isEmpty()) {
			message = "Wrong formula name was asked.";
		}
		else {
			content.addAll(List.of(formula.split("\n")));
			message = "Formula was appended.";
		}
		Payload payload = new Payload(
				null,
				content,
				editExistingPage,
				fileName,
				null,
				message,
				null,
				null,
				title,
				null
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView savePage(String initialView,
								 String title,
								 String fileName,
								 List<String> content,
								 Boolean editExistingPage) {
		String message;
		if (title == null || title.isBlank()) {
			title = "";
			message = "Please define a title.";
		}
		else if (fileName == null || fileName.isBlank()) {
			fileName = "";
			message = "Please define a file name.";
		}
		else {
			if (content == null) {
				content = new ArrayList<>();
			}
			if (editExistingPage == null) {
				editExistingPage = false;
			}
			Optional<TitleEntity> optionalTitleEntity = titleRepository.findByTitle(title);
			if (editExistingPage) {
				if (optionalTitleEntity.isEmpty()) {
					message = "There is no existing page with this title.";
				}
				else {
					txtRepository.deleteById(optionalTitleEntity.get().getTxtId());
					htmlRepository.deleteById(optionalTitleEntity.get().getHtmlId());
					titleRepository.deleteById(optionalTitleEntity.get().getId());
					HtmlEntity htmlEntity = htmlRepository.save(new HtmlEntity(new ArrayList<>(), new ArrayList<>()));
					TxtEntity txtEntity = txtRepository.save(new TxtEntity(processRecords.listToString(content)));
					titleRepository.save(new TitleEntity(title, fileName, txtEntity.getId(), htmlEntity.getId()));
					message = "Source page has been overwritten.";
				}
			}
			else { //editExistingPage == false
				if (optionalTitleEntity.isPresent()) {
					message = "There is an existing page with this title.";
				}
				else {
					HtmlEntity htmlEntity = htmlRepository.save(new HtmlEntity(new ArrayList<>(), new ArrayList<>()));
					TxtEntity txtEntity = txtRepository.save(new TxtEntity(processRecords.listToString(content)));
					titleRepository.save(new TitleEntity(title, fileName, txtEntity.getId(), htmlEntity.getId()));
					editExistingPage = true;
					message = "Source page has been saved.";
				}
			}
		}
		Payload payload = new Payload(
				null,
				content,
				editExistingPage,
				fileName,
				null,
				message,
				null,
				null,
				title,
				null
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView importTxt(String initialView, String fileNames, Boolean confirm) {
		if (fileNames == null || fileNames.isBlank() || confirm == null || !confirm) {
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					"Please upload minimum one file and confirm source overwriting.",
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView(initialView, "payload", payload);
		}
		List<File> files = Stream.of(fileNames.split(fileOperations.getOSPathSeparator()))
				.map(File::new)
				.toList();
		List<File> notImportedFiles = processRecords.importTxtFiles(
				files,
				titleRepository,
				txtRepository,
				htmlRepository,
				fileOperations,
				formulas,
				extractors);
		List<String> titles = processRecords.getAllTitles(titleRepository);
		Payload payload = new Payload(
				false,
				null,
				null,
				null,
				"",
				notImportedFiles.size() + " of " + files.size() + " files were not imported.",
				null,
				null,
				null,
				titles
		);
		return new ModelAndView(initialView, "payload", payload);
	}

	public ModelAndView generateHtml(String initialView, Boolean confirm) {
		if (confirm == null || !confirm) {
			Payload payload = new Payload(
					false,
					null,
					null,
					null,
					"",
					"Please confirm generating pages.",
					null,
					null,
					null,
					processRecords.getAllTitles(titleRepository)
			);
			return new ModelAndView(initialView, "payload", payload);
		}
		long[] messageData = processRecords.generate(
				titleRepository,
				txtRepository,
				htmlRepository,
				formulas,
				processPage,
				extractors,
				htmlGenerators);
		Payload payload = new Payload(
				false,
				null,
				null,
				null,
				"",
				messageData[0] + " pages in " + messageData[1] + " seconds has been processed.",
				null,
				null,
				null,
				processRecords.getAllTitles(titleRepository)
		);
		return new ModelAndView(initialView, "payload", payload);
	}
}

//TODO check all Stream operations for being parallel and its necessity.
//TODO exportTxt()
