package com.example.smartfridge_app_finalproject.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.CategoryRepository
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AddProductManualFragment : Fragment() {

    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var expiryDateEdit: TextInputEditText
    private lateinit var productNameEdit: TextInputEditText
    private lateinit var quantityEdit: TextInputEditText
    private lateinit var productBarCodeEdit: TextInputEditText
    private lateinit var addButton: MaterialButton
    private var inventory = InventoryManager()
    private val validInputManager = ValidInputManager.getInstance()
    private val categoryRepository = CategoryRepository()
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product_manual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        initViews()
        setupCategoryDropdown()
        setupDatePicker()
    }

    private fun setupViews(view: View) {
        categoryDropdown = view.findViewById(R.id.add_product_manual_ACTV_category)
        categoryLayout = view.findViewById(R.id.add_product_manual_TIL_category)
        productBarCodeEdit = view.findViewById(R.id.add_product_manual_EDT_barcode)
        expiryDateEdit = view.findViewById(R.id.add_product_manual_EDT_expiry)
        productNameEdit = view.findViewById(R.id.add_product_manual_EDT_name)
        quantityEdit = view.findViewById(R.id.add_product_manual_EDT_quantity)
        addButton = view.findViewById(R.id.add_product_manual_BTN_add)
    }

    private fun initViews() {
        // הגדרת מאזינים והתנהגויות התחלתיות
        addButton.setOnClickListener {
            handleAddProduct()
        }

        // ניקוי שגיאות בעת הקלדה
        productNameEdit.setOnClickListener { productNameEdit.error = null }
        quantityEdit.setOnClickListener { quantityEdit.error = null }
        categoryDropdown.setOnClickListener { categoryLayout.error = null }
        expiryDateEdit.setOnClickListener {
            expiryDateEdit.error = null
            showDatePicker()
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        //Barcode
        val barcode = productBarCodeEdit.text.toString()
        if (!validInputManager.isValidBarcode(barcode)) {
            productBarCodeEdit.error = getString(R.string.valid_input_product_barcode)
            isValid = false
        }

        //Product name
        val productName = productNameEdit.text.toString()
        if (!validInputManager.isValidProductName(productName)) {
            productNameEdit.error = getString(R.string.valid_input_product_name)
            isValid = false
        }

        //Category
        val category = categoryDropdown.text.toString()
        if (!validInputManager.isValidCategory(category)) {
            categoryLayout.error =
                getString(R.string.valid_input_please_select_a_category_from_the_list)
            isValid = false
        }

        //Quantity
        val quantity = quantityEdit.text.toString().toIntOrNull() ?: 0
        if (!validInputManager.isValidQuantity(quantity)) {
            quantityEdit.error = getString(R.string.valid_input_quantity_must_be_between_and)
            isValid = false
        }

        //Expiry Date
        val expiryDate = expiryDateEdit.text.toString()
        if (!validInputManager.isValidExpiryDate(expiryDate)) {
            expiryDateEdit.error =
                getString(R.string.valid_input_please_select_a_valid_expiration_date)
            isValid = false
        }

        return isValid
    }

    private fun handleAddProduct() {
        if (!validateFields()) {
            return
        }

        val barCode = productBarCodeEdit.text.toString()
        val productName = productNameEdit.text.toString()
        val category = categoryDropdown.text.toString()
        val quantity = quantityEdit.text.toString().toIntOrNull() ?: 0
        val expiryDate = expiryDateEdit.text.toString()

        // TODO: Add logic to save product
        val product = Product(
            barCode = barCode,
            name = productName,
            imageUrl = R.drawable.category_no_picture,
            category = category,
            quantity = quantity,
            expiryDate = expiryDate
        )
        if (inventory.addProduct(product)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.added_successfully, product.name), Toast.LENGTH_SHORT
            ).show()
            // TODO: You might want to clear fields or navigate back
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.product_not_added), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = categoryRepository.getInitialCategories()
        val categoryNames = categories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        categoryDropdown.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        expiryDateEdit.setOnClickListener {
            expiryDateEdit.error = null
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                //Clear the error (if any) when a new date is selected
                expiryDateEdit.error = null
                updateDateInView()
            },

            //These three parameters determine the start date that will be displayed in the dialog
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateInView() {
        expiryDateEdit.setText(dateFormatter.format(calendar.time))
    }
}